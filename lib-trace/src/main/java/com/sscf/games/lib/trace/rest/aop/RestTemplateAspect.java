package com.sscf.games.lib.trace.rest.aop;

import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceOrderConstants;
import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

/**
 * 结合{@link TraceRestTemplateInterceptor}，对restTemplate进行拦截，记录时间和结果
 *
 * @author jianlong_li
 * @date 2018/11/17 12:11
 */
@Aspect
@Order(TraceOrderConstants.REST_AOP_ORDER)
public class RestTemplateAspect {

    @Around("execution(* org.springframework.web.client.RestOperations.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        TraceContext context = TraceContext.getContext();
        TraceSpan traceSpan = context.initClientSpan(TraceTypeEnum.HTTP_CLIENT);
        traceSpan.addTagMap(ObjectUtil.buildArgsMap(joinPoint.getArgs()));
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        traceSpan.addTag(TraceTagConstants.REST_METHOD, method.getName());

        traceSpan.startCount();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            traceSpan.stopCount();
            Throwable unWarpException = ExceptionUtil.unWarp(e);
            if (ExceptionUtil.containsExceptionType(unWarpException, SocketTimeoutException.class)) {
                context.endTrace(traceSpan, TraceResultTypeEnum.TIMEOUT, unWarpException);
            } else {
                context.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, unWarpException);
            }
            throw e;
        }
        traceSpan.stopCount();
        context.endTraceByResult(traceSpan, result);
        return result;
    }
}
