package com.sscf.games.lib.trace.redis.aop;

import com.sscf.games.lib.trace.common.ServicePoint;
import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import com.sscf.games.lib.trace.redis.RedisServer;
import com.sscf.games.lib.trace.redis.config.RedisTraceConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

/**
 * ValueOperations的代理，用于记录序列化前的入参、反序列化后的出参
 *
 * @author jianlong_li
 * @date 2018/11/15 10:26
 */
@Slf4j
public class RedisOperationsHandler implements InvocationHandler {

    private Object operationsTarget;

    public RedisOperationsHandler(Object operationsTarget) {
        this.operationsTarget = operationsTarget;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Object的方法直接放行
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(operationsTarget, args);
        }
        TraceContext traceContext = TraceContext.getContext();
        TraceSpan traceSpan = this.initRedisTraceSpan(traceContext, ObjectUtil.getMethodName(method), RedisTraceConfig.getRedisServer());
        traceSpan.addTagMap(ObjectUtil.buildArgsMap(args));

        traceSpan.startCount();
        Object result;
        try {
            result = method.invoke(operationsTarget, args);
        } catch (Exception e) {
            traceSpan.stopCount();
            Throwable targetException = ExceptionUtil.unWarp(e);
            if (ExceptionUtil.containsExceptionType(targetException, SocketTimeoutException.class)) {
                traceContext.endTrace(traceSpan, TraceResultTypeEnum.EXCEPTION, targetException);
            } else {
                traceContext.endTrace(traceSpan, TraceResultTypeEnum.TIMEOUT, targetException);
            }
            throw e;
        } finally {
            RedisTemplateAspect.clearDoRedisCommandFlag();
        }
        traceSpan.stopCount();
        traceContext.endTraceByResult(traceSpan, result);
        return result;
    }

    private TraceSpan initRedisTraceSpan(TraceContext traceContext, String methodName, RedisServer redisServer) {
        TraceSpan traceSpan = traceContext.initClientSpan(TraceTypeEnum.REDIS_CLIENT);
        traceSpan.setName(methodName);
        traceSpan.setRemotePoint(new ServicePoint(TraceContext.REDIS_SERVICE_NAME, redisServer.getHost(), redisServer.getPort()));
        return traceSpan;
    }


}
