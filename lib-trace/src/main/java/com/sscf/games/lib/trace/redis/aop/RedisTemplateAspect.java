package com.sscf.games.lib.trace.redis.aop;

import com.sscf.games.lib.trace.common.ServicePoint;
import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceOrderConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import com.sscf.games.lib.trace.redis.RedisServer;
import com.sscf.games.lib.trace.redis.config.RedisTraceConfig;
import com.sscf.games.lib.trace.redis.util.RedisAopUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author jianlong_li
 * @date 2018/11/15 21:09
 */
@Aspect
@Order(TraceOrderConstants.REDIS_AOP_ORDER)
@Slf4j
public class RedisTemplateAspect {

    // 已经加载代理的OPS get方法
    private static final Set<Method> LOAD_OPS_METHOD_SET = new HashSet<>();

    // <OPS get方法, OPS的变量>
    private static final Map<Method, Field> OPS_METHOD_FIELD_MAP = new HashMap<>();

    // 本线程是否做过redis操作，用于记录区分redisTemplate本身的一些方法是否真的时redis操作（需要记录Trace）
    private static final ThreadLocal<Boolean> DO_REDIS_COMMAND_FLAG = new ThreadLocal<>();

    static {
        addOpsMethodField("opsForValue", "valueOps");
        addOpsMethodField("opsForList", "listOps");
        addOpsMethodField("opsForSet", "setOps");
        addOpsMethodField("opsForZSet", "zSetOps");
        addOpsMethodField("opsForGeo", "geoOps");
        addOpsMethodField("opsForHyperLogLog", "hllOps");
        OPS_METHOD_FIELD_MAP.put(ReflectionUtils.findMethod(RedisTemplate.class, "opsForCluster"), null);
    }

    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object doAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        RedisTemplate redisTemplate = (RedisTemplate) joinPoint.getTarget();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (OPS_METHOD_FIELD_MAP.keySet().contains(method)) {
            return this.getOpsProxy(redisTemplate, method, joinPoint);
        }

        TraceContext traceContext = TraceContext.getContext();
        RedisServer redisServer = RedisTraceConfig.getRedisServer();
        TraceSpan traceSpan = initRedisTraceSpan(traceContext, ObjectUtil.getMethodName(method), redisServer);

        traceSpan.startCount();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            traceSpan.stopCount();
            Throwable unWarpException = ExceptionUtil.unWarp(e);
            if (ExceptionUtil.containsExceptionType(unWarpException, SocketTimeoutException.class)) {
                this.tryEndTrace(traceContext, traceSpan, TraceResultTypeEnum.TIMEOUT, null, unWarpException);
            } else {
                this.tryEndTrace(traceContext, traceSpan, TraceResultTypeEnum.EXCEPTION, null, unWarpException);
            }
            throw e;
        }
        traceSpan.stopCount();
        this.tryEndTrace(traceContext, traceSpan, TraceResultTypeEnum.SUCCESS, result, null);
        return result;
    }

    private TraceSpan initRedisTraceSpan(TraceContext traceContext, String methodName, RedisServer redisServer) {
        TraceSpan traceSpan = traceContext.initClientSpan(TraceTypeEnum.REDIS_CLIENT);
        traceSpan.setName(methodName);
        traceSpan.setRemotePoint(new ServicePoint(TraceContext.REDIS_SERVICE_NAME, redisServer.getHost(), redisServer.getPort()));
        return traceSpan;
    }

    /**
     * 尝试发送Trace（如果当前线程有做redis操作的话）
     */
    private void tryEndTrace(TraceContext traceContext, TraceSpan traceSpan,
                             TraceResultTypeEnum traceResultTypeEnum, Object result, Throwable throwable) {
        try {
            if (!Boolean.TRUE.equals(DO_REDIS_COMMAND_FLAG.get())) {
                traceContext.pollSpan(traceSpan);
                return;
            }
            if (traceResultTypeEnum == TraceResultTypeEnum.SUCCESS) {
                traceContext.endTraceByResult(traceSpan, result);
                return;
            }
            traceContext.endTrace(traceSpan, traceResultTypeEnum, throwable);
        } finally {
            clearDoRedisCommandFlag();
        }
    }

    /**
     * 获取valueOps/ListOps等的代理类
     * 1.判断是否未redisTemplate的全局变量，如果不是，每次都new一个新的代理
     * 2.如果是全局变量，判断是否已经加载过这个代理了，是则返回结果
     * 3.如果加载过，进行加载,然后通过反射往restTemplate设置值，返回代理
     */
    private Object getOpsProxy(RedisTemplate redisTemplate, Method method, ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        Field field = OPS_METHOD_FIELD_MAP.get(method);
        // 没有全局变量，每次都new
        if (field == null) {
            return RedisAopUtil.newOperationsProxy(result);
        }
        // 全局变量已经load过了
        if (LOAD_OPS_METHOD_SET.contains(method)) {
            return result;
        }
        // 双重判断锁保证单例（防止多实例引发不可控的问题）
        synchronized (LOAD_OPS_METHOD_SET) {
            if (!LOAD_OPS_METHOD_SET.contains(method)) {
                ValueOperations operationsProxy = RedisAopUtil.newOperationsProxy(result);
                ReflectionUtils.setField(field, redisTemplate, operationsProxy);
                LOAD_OPS_METHOD_SET.add(method);
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 记录本线程有做redis操作
     */
    public static void addRedisCommandFlag() {
        DO_REDIS_COMMAND_FLAG.set(Boolean.TRUE);
    }

    /**
     * 去除本线程有做redis操作标志
     */
    public static void clearDoRedisCommandFlag() {
        DO_REDIS_COMMAND_FLAG.remove();
    }

    private static void addOpsMethodField(String methodName, String fieldName) {
        Class<RedisTemplate> redisTemplateClass = RedisTemplate.class;
        Method method = ReflectionUtils.findMethod(redisTemplateClass, methodName);
        Field field = ReflectionUtils.findField(redisTemplateClass, fieldName);
        if (field != null && method != null) {
            field.setAccessible(true);
            OPS_METHOD_FIELD_MAP.put(method, field);
        } else {
            log.warn("[调用链] 初始化redis切面，获取对应的method或field，method={},field={}", method, fieldName);
        }

    }
}
