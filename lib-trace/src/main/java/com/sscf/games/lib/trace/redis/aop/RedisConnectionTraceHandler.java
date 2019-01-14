package com.sscf.games.lib.trace.redis.aop;

import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于记录redis具体执行的命令；集群时，可以用于获取对应的ip（暂未开发）
 *
 * @author jianlong_li
 * @date 2018/11/13 10:15
 */
@Slf4j
public class RedisConnectionTraceHandler implements InvocationHandler {
    private static final Set<Class> REDIS_COMMANDS_CLASS_SET;
    private Object target;

    static {
        REDIS_COMMANDS_CLASS_SET = new HashSet<>();
        REDIS_COMMANDS_CLASS_SET.add(RedisCommands.class);
        REDIS_COMMANDS_CLASS_SET.addAll(Arrays.asList(RedisCommands.class.getInterfaces()));
    }

    public RedisConnectionTraceHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (!REDIS_COMMANDS_CLASS_SET.contains(method.getDeclaringClass())) {
                return method.invoke(target, args);
            }
            RedisTemplateAspect.addRedisCommandFlag();

            TraceContext traceContext = TraceContext.getContext();
            TraceSpan traceSpan = traceContext.peekSpan(TraceTypeEnum.REDIS_CLIENT);
            if (traceSpan != null) {
                traceSpan.addTag(TraceTagConstants.REDIS_COMMAND, method.getName());
            } else {
                log.error("[调用链] 本次方法未做拦截，method:{}", method);
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unWarp(e);
        }
    }
}
