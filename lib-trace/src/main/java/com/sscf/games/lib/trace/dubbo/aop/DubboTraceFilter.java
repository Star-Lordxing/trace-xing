package com.sscf.games.lib.trace.dubbo.aop;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.sscf.games.lib.trace.common.ServicePoint;
import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceHeader;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceOrderConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = TraceOrderConstants.DUBBO_FILTER_ORDER)
@Slf4j
public class DubboTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        TraceContext traceContext = TraceContext.getContext();
        TraceSpan traceSpan;
        if (isConsumerSide(invoker)) {
            traceSpan = initDubboConsumerSpan(traceContext, invoker, invocation);
            // 消费者，加入Trace头部
            invocation.getAttachments().putAll(TraceHeader.newByTraceSpan(traceSpan).toMap());
        } else {
            // 提供者，尝试解析Trace头部
            traceSpan = this.initDubboProviderSpan(traceContext, invoker, invocation);
        }

        traceSpan.startCount();
        Result invokeResult;
        try {
            invokeResult = invoker.invoke(invocation);
        } catch (RpcException e) {
            traceSpan.stopCount();
            if (e.isTimeout()) {
                traceContext.endTrace(traceSpan, TraceResultTypeEnum.TIMEOUT, e.getCause());
            } else {
                traceContext.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, e);
            }
            throw e;
        } catch (Exception e) {
            traceSpan.stopCount();
            traceContext.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, e);
            throw e;
        }

        traceSpan.stopCount();
        if (invokeResult == null || invokeResult.hasException()) {
            Throwable throwable = invokeResult != null ? invokeResult.getException() : new NullPointerException("rpc执行结果为null");
            traceContext.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, throwable);
            return invokeResult;
        }

        traceContext.endTraceByResult(traceSpan, invokeResult.getValue());
        return invokeResult;
    }

    /**
     * 初始化dubbo消费者traceSpan
     */
    private TraceSpan initDubboConsumerSpan(TraceContext traceContext, Invoker<?> invoker, Invocation invocation) {
        TraceSpan traceSpan = traceContext.initClientSpan(TraceTypeEnum.RPC_CONSUMER);
        traceSpan.setName(this.getDubboSpanName(invoker.getInterface(), invocation.getMethodName()));
        traceSpan.setRemotePoint(new ServicePoint(null, invoker.getUrl().getIp(), invoker.getUrl().getPort()));
        traceSpan.addTagMap(ObjectUtil.buildArgsMap(invocation.getArguments()));
        return traceSpan;
    }

    /**
     * 初始化dubbo提供者traceSpan
     */
    private TraceSpan initDubboProviderSpan(TraceContext traceContext, Invoker<?> invoker, Invocation invocation) {
        // 尝试解析头部，如果没有则生成新的traceId
        TraceHeader traceHeader = TraceHeader.newByRpcInvocation(invocation);
        TraceSpan traceSpan = traceContext.initServerSpan(TraceTypeEnum.RPC_PRODUCER, traceHeader);
        traceSpan.setName(this.getDubboSpanName(invoker.getInterface(), invocation.getMethodName()));
        traceSpan.setRemotePoint(new ServicePoint(traceHeader.getServiceName(), traceHeader.getHost(), traceHeader.getPort()));
        return traceSpan;
    }

    private boolean isConsumerSide(Invoker<?> invoker) {
        return invoker.getUrl().getParameter(Constants.SIDE_KEY, Constants.PROVIDER_SIDE).equals(Constants.CONSUMER_SIDE);
    }

    private String getDubboSpanName(Class interfaceClass, String methodName) {
        return interfaceClass.getName() + "#" + methodName;
    }
}
