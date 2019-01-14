package com.sscf.games.lib.trace.servlet.aop;

import com.sscf.games.lib.trace.common.ServicePoint;
import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceHeader;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceOrderConstants;
import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import com.sscf.games.lib.trace.common.util.IpAddressUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author jianlong_li
 * @date 2018/11/17 16:35
 */
@Aspect
@Order(TraceOrderConstants.SERVLET_AOP_ORDER)
@Slf4j
public class TraceServletAspect {

    private static final String ERROR_PAGE = "/error";

    @Around("pointcut()")
    public Object doRequestReturn(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            log.warn("[调用链] servlet切面获取不到request, method={}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        }
        HttpServletRequest httpServletRequest = attrs.getRequest();
        // ERROR页面不做记录
        if (ERROR_PAGE.equalsIgnoreCase(httpServletRequest.getRequestURI())) {
            return joinPoint.proceed();
        }
        TraceContext traceContext = TraceContext.getContext();
        TraceSpan traceSpan = this.initServletSpan(traceContext, httpServletRequest);
        traceSpan.startCount();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            traceSpan.stopCount();
            traceSpan.addTag(TraceTagConstants.HTTP_RESP_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            traceContext.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, ExceptionUtil.unWarp(e));
            throw e;
        }
        traceSpan.stopCount();

        HttpServletResponse response = attrs.getResponse();
        if (response != null) {
            traceSpan.addTag(TraceTagConstants.HTTP_RESP_CODE, String.valueOf(response.getStatus()));
        }
        traceContext.endTraceByResult(traceSpan, result);
        return result;
    }

    private TraceSpan initServletSpan(TraceContext traceContext, HttpServletRequest servletRequest) {
        TraceHeader traceHeader = TraceHeader.newByServletRequest(servletRequest);
        TraceSpan traceSpan = traceContext.initServerSpan(TraceTypeEnum.HTTP_SERVER, traceHeader);
        traceSpan.setName(servletRequest.getRequestURL().toString());

        ServicePoint remotePoint = new ServicePoint(traceHeader.getServiceName(),
                IpAddressUtil.getRequestIp(servletRequest), servletRequest.getRemotePort());
        traceSpan.setRemotePoint(remotePoint);

        traceSpan.addTagMap(this.getParamMap(servletRequest));
        Map<String, String> headerMap = this.getNotTraceHeaderMap(servletRequest);
        traceSpan.addTag(TraceTagConstants.HTTP_REQ_HEADER, Objects.toString(traceContext.filterHttpHeader(headerMap)));
        traceSpan.addTag(TraceTagConstants.HTTP_REQ_METHOD, servletRequest.getMethod());
        return traceSpan;
    }

    /**
     * 获取不包含Trace请求头的头部
     */
    private Map<String, String> getNotTraceHeaderMap(HttpServletRequest httpServletRequest) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerKey = headerNames.nextElement();
            if (!TraceHeader.isTraceHeaderKey(headerKey)) {
                String header = httpServletRequest.getHeader(headerKey);
                headerMap.put(headerKey, header);
            }
        }
        return headerMap;
    }

    /**
     * 获取请求参数
     */
    private Map<String, String> getParamMap(HttpServletRequest httpServletRequest) {
        Map<String, String> paramsMap = new HashMap<>();
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        for (Map.Entry<String, String[]> headerEntry : parameterMap.entrySet()) {
            paramsMap.put("param." + headerEntry.getKey(), Arrays.toString(headerEntry.getValue()));
        }
        return paramsMap;
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.Mapping)" +
            " || @annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PutMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PatchMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public void pointcut() {
        // do nothing
    }
}
