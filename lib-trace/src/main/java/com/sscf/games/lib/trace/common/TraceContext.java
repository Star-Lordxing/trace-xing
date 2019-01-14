package com.sscf.games.lib.trace.common;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.service.TraceSendService;
import com.sscf.games.lib.trace.common.util.TraceIdUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * 调用链上下文，负责开启、结束当个调用Span；持有调用栈对象（每个线程）
 *
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Getter
@Slf4j
public class TraceContext implements ApplicationContextAware {

    public static final String REDIS_SERVICE_NAME = "redis_server";
    public static final Integer HTTPS_PORT = 443;
    private static final Integer HTTP_PORT = 80;
    private static TraceContext traceContext;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port:8080}")
    private Integer serverPort;

    private String localIP = NetUtils.getLocalHost();

    private Integer dubboPort = 20080;

    /**
     * 当前线程的调用栈
     */
    private final ThreadLocal<Deque<TraceSpan>> spanStackThreadLocal = new ThreadLocal<>();

    @Resource
    private TraceSendService traceSendService;

    @Resource
    private TraceResultValidator traceResultValidator;

    @Resource
    private HttpHeaderFilter httpHeaderFilter;

    /**
     * 根据类型获取客户端Span
     * 1.从栈顶获取元素，如果存在则认为时父节点，获取对应的traceId和parentId
     * 2.如果不存在，则认为当前节点为第一个节点，生成traceId
     *
     * @param typeEnum 客户端类型（client）
     */
    public TraceSpan initClientSpan(TraceTypeEnum typeEnum) {
        TraceSpan traceSpan = new TraceSpan(typeEnum);
        TraceSpan parentSpan = this.peekSpan();
        // 如果存在父Span
        if (parentSpan != null) {
            traceSpan.setTraceId(parentSpan.getTraceId());
            traceSpan.setParentId(parentSpan.getId());
        } else {
            traceSpan.setTraceId(TraceIdUtil.generateTraceId());
            traceSpan.setParentId(TraceIdUtil.generate16Id());
        }
        traceSpan.setId(TraceIdUtil.generate16Id());

        // 本地节点
        int port = 0;
        if (typeEnum == TraceTypeEnum.HTTP_CLIENT) {
            port = HTTP_PORT;
        } else if (typeEnum == TraceTypeEnum.RPC_CONSUMER) {
            port = dubboPort;
        }
        traceSpan.setLocalPoint(new ServicePoint(serviceName, localIP, port));

        this.pushSpan(traceSpan);
        return traceSpan;
    }

    /**
     * 根据类型获取客户端Span
     * 1.TraceHeader获取traceId，如果存在则认为是客户端传送过来的，直接获取
     * 2.如果不存在，则认为当前节点为第一个节点，生成traceId
     *
     * @param typeEnum 服务端端类型（server）
     */
    public TraceSpan initServerSpan(TraceTypeEnum typeEnum, TraceHeader traceHeader) {
        TraceSpan traceSpan = new TraceSpan(typeEnum);
        if (!StringUtils.isEmpty(traceHeader.getTraceId())) {
            traceSpan.setTraceId(traceHeader.getTraceId());
            traceSpan.setParentId(traceHeader.getParentId());
        } else {
            traceSpan.setTraceId(TraceIdUtil.generateTraceId());
            traceSpan.setParentId(TraceIdUtil.generate16Id());
        }
        traceSpan.setId(TraceIdUtil.generate16Id());

        int port = 0;
        if (typeEnum == TraceTypeEnum.HTTP_SERVER) {
            port = serverPort;
        } else if (typeEnum == TraceTypeEnum.RPC_PRODUCER) {
            port = dubboPort;
        }
        traceSpan.setLocalPoint(new ServicePoint(serviceName, localIP, port));

        this.pushSpan(traceSpan);
        return traceSpan;
    }

    /**
     * Trace至此生命周期结束，记录结果(Tag)，校验结果，发送Trace
     * 校验结果为false则置为ERROR_RESULT，否则SUCCESS
     */
    public void endTraceByResult(TraceSpan traceSpan, Object result) {
        traceSpan.addTag(TraceTagConstants.RESULT, result);
        TraceResultTypeEnum resultTypeEnum = TraceResultTypeEnum.SUCCESS;
        if (!traceResultValidator.validate(traceSpan, result)) {
            resultTypeEnum = TraceResultTypeEnum.ERROR_RESULT;
        }
        this.endTrace(traceSpan, resultTypeEnum, null);
    }

    /**
     * Trace至此生命周期结束，发送Trace；
     * 一般用于异常情况
     */
    public void endTrace(TraceSpan traceSpan, TraceResultTypeEnum typeEnum, Throwable exception) {
        try {
            traceSpan.setResultTypeEnum(TraceResultTypeEnum.maxPriority(traceSpan.getResultTypeEnum(), typeEnum));
            traceSpan.setException(exception);
            traceSendService.send(traceSpan);
        } catch (Exception e) {
            log.error("[调用链] 异常, span={}", traceSpan, traceSpan.getId(), e);
        } finally {
            // 去除栈顶(当前)元素
            this.pollSpan(traceSpan);
        }
    }

    /**
     * 弹出Span
     */
    public void pollSpan(TraceSpan traceSpan) {
        Deque<TraceSpan> traceSpanStack = spanStackThreadLocal.get();
        if (traceSpanStack != null) {
            if (traceSpan.equals(traceSpanStack.peek())) {
                traceSpanStack.poll();
            }
            if (traceSpanStack.isEmpty()) {
                spanStackThreadLocal.remove();
            }
        }
    }

    /**
     * 入栈Span
     */
    private void pushSpan(TraceSpan nowSpan) {
        Deque<TraceSpan> traceSpanStack = this.spanStackThreadLocal.get();
        if (traceSpanStack == null) {
            traceSpanStack = new LinkedList<>();
            this.spanStackThreadLocal.set(traceSpanStack);
        }
        traceSpanStack.push(nowSpan);
    }

    /**
     * 尝试获取指定类型的栈顶元素
     *
     * @return 如果栈顶元素不为空且和指定类型一致，返回栈顶元素；否则null
     */
    public TraceSpan peekSpan(TraceTypeEnum traceTypeEnum) {
        TraceSpan traceSpan = this.peekSpan();
        if (traceSpan != null && traceSpan.getTraceTypeEnum() == traceTypeEnum) {
            return traceSpan;
        }
        return null;
    }

    /**
     * 获取当前栈顶的Span
     */
    public TraceSpan peekSpan() {
        Deque<TraceSpan> traceSpans = spanStackThreadLocal.get();
        if (traceSpans == null) {
            return null;
        }
        return traceSpans.peek();
    }

    public Map<String, String> filterHttpHeader(Map<String, String> headerMap) {
        return httpHeaderFilter.filter(headerMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Map<String, ProtocolConfig> protocolMap = applicationContext.getBeansOfType(ProtocolConfig.class);
        for (Map.Entry<String, ProtocolConfig> configEntry : protocolMap.entrySet()) {
            Integer port = configEntry.getValue().getPort();
            if (port != null) {
                this.dubboPort = port;
                break;
            }
        }
        traceContext = this;
    }


    public static TraceContext getContext() {
        return traceContext;
    }

}