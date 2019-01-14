package com.sscf.games.lib.trace.common;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcContext;
import com.sscf.games.lib.trace.common.util.TraceIdUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Data
@NoArgsConstructor
public class TraceHeader {
    public static final String FINANCE_TRACE_ID = "finance_trace_id";
    public static final String FINANCE_PARENT_ID = "finance_parent_id";

    public static final String FINANCE_REMOTE_SERVICE_NAME = "finance_remote_service_name";
    public static final String FINANCE_REMOTE_HOST = "finance_remote_host";
    public static final String FINANCE_REMOTE_PORT = "finance_remote_port";

    private String traceId;
    private String parentId;
    private String serviceName;
    private String host;
    private Integer port;

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(FINANCE_TRACE_ID, traceId);
        map.put(FINANCE_PARENT_ID, parentId);
        map.put(FINANCE_REMOTE_SERVICE_NAME, serviceName);
        map.put(FINANCE_REMOTE_HOST, host);
        map.put(FINANCE_REMOTE_PORT, String.valueOf(port));
        return map;
    }

    /**
     * 判断是否是专属的头部key
     */
    public static boolean isTraceHeaderKey(String keyStr) {
        if (StringUtils.isEmpty(keyStr)) {
            return false;
        }
        return FINANCE_TRACE_ID.equals(keyStr) || FINANCE_PARENT_ID.equals(keyStr) || FINANCE_REMOTE_SERVICE_NAME.equals(keyStr) ||
                FINANCE_REMOTE_HOST.equals(keyStr) || FINANCE_REMOTE_PORT.equals(keyStr);
    }

    /**
     * 根据servletRequest解析获取请求头，如果没有则初始化一个
     */
    public static TraceHeader newByServletRequest(HttpServletRequest httpServletRequest) {
        TraceHeader traceHeader = new TraceHeader();
        traceHeader.traceId = httpServletRequest.getHeader(FINANCE_TRACE_ID);
        traceHeader.parentId = httpServletRequest.getHeader(FINANCE_PARENT_ID);
        traceHeader.serviceName = httpServletRequest.getHeader(FINANCE_REMOTE_SERVICE_NAME);
        if (StringUtils.isEmpty(traceHeader.traceId)) {
            traceHeader.traceId = TraceIdUtil.generateTraceId();
            traceHeader.parentId = TraceIdUtil.FIRST_SPAN_PARENT_ID;
        }
        return traceHeader;
    }

    /**
     * 根据rpc的Invocation尝试获取Trace头，如果没有则初始化一个
     */
    public static TraceHeader newByRpcInvocation(Invocation invocation) {
        Map<String, String> traceHeaderMap = invocation.getAttachments();
        TraceHeader traceHeader = new TraceHeader();
        traceHeader.traceId = traceHeaderMap.get(FINANCE_TRACE_ID);
        if (!StringUtils.isEmpty(traceHeader.traceId)) {
            traceHeader.parentId = traceHeaderMap.get(FINANCE_PARENT_ID);
            traceHeader.serviceName = traceHeaderMap.get(FINANCE_REMOTE_SERVICE_NAME);
        } else {
            traceHeader.traceId = TraceIdUtil.generateTraceId();
            traceHeader.parentId = TraceIdUtil.FIRST_SPAN_PARENT_ID;
        }

        traceHeader.host = traceHeaderMap.get(FINANCE_REMOTE_HOST);
        if (StringUtils.isEmpty(traceHeader.host)) {
            traceHeader.host = RpcContext.getContext().getRemoteHost();
        }

        String portStr = traceHeaderMap.get(FINANCE_REMOTE_PORT);
        if (!StringUtils.isEmpty(portStr)) {
            traceHeader.port = Integer.valueOf(portStr);
        } else {
            traceHeader.port = RpcContext.getContext().getRemotePort();
        }
        return traceHeader;
    }

    /**
     * 获取指定span的请求头
     */
    public static TraceHeader newByTraceSpan(TraceSpan traceSpan) {
        TraceHeader traceHeader = new TraceHeader();
        traceHeader.traceId = traceSpan.getTraceId();
        traceHeader.parentId = traceSpan.getId();
        traceHeader.serviceName = traceSpan.getLocalPoint().getServiceName();
        traceHeader.host = traceSpan.getLocalPoint().getHost();
        traceHeader.port = traceSpan.getLocalPoint().getPort();
        return traceHeader;
    }

}
