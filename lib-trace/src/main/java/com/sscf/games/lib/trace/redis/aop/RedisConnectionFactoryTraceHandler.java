package com.sscf.games.lib.trace.redis.aop;

import com.sscf.games.lib.trace.common.util.ExceptionUtil;
import com.sscf.games.lib.trace.redis.util.RedisAopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 当获取Connection时，认为redis操作才会触发，这时候记录一下{@link RedisTemplateAspect}
 *
 * @author jianlong_li
 * @date 2018/11/13 10:22
 */
@Slf4j
public class RedisConnectionFactoryTraceHandler implements InvocationHandler {

    private RedisConnectionFactory target;

    private static final Set<Method> GET_CONNECTION_METHOD_SET = new HashSet<>();

    static {
        GET_CONNECTION_METHOD_SET.add(ReflectionUtils.findMethod(RedisConnectionFactory.class, "getConnection"));
        GET_CONNECTION_METHOD_SET.add(ReflectionUtils.findMethod(RedisConnectionFactory.class, "getClusterConnection"));
        GET_CONNECTION_METHOD_SET.add(ReflectionUtils.findMethod(RedisConnectionFactory.class, "getSentinelConnection"));
    }

    public RedisConnectionFactoryTraceHandler(RedisConnectionFactory target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (GET_CONNECTION_METHOD_SET.contains(method)) {
                return RedisAopUtil.newRedisConnectionProxy(method.invoke(target, args));
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unWarp(e);
        }
    }
}
