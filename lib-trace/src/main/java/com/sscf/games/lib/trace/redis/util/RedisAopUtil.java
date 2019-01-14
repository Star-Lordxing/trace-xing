package com.sscf.games.lib.trace.redis.util;

import com.sscf.games.lib.trace.redis.aop.RedisConnectionFactoryTraceHandler;
import com.sscf.games.lib.trace.redis.aop.RedisConnectionTraceHandler;
import com.sscf.games.lib.trace.redis.aop.RedisOperationsHandler;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Proxy;

/**
 * @author jianlong_li
 * @date 2018/11/13 10:41
 */
public class RedisAopUtil {
    private RedisAopUtil() {
    }

    public static RedisConnectionFactory newRedisConnectionFactoryProxy(RedisConnectionFactory delegate) {
        return (RedisConnectionFactory) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class[]{RedisConnectionFactory.class}, new RedisConnectionFactoryTraceHandler(delegate));
    }

    public static RedisConnection newRedisConnectionProxy(Object delegate) {
        return (RedisConnection) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class[]{RedisConnection.class}, new RedisConnectionTraceHandler(delegate));
    }

    public static ValueOperations newOperationsProxy(Object delegate) {
        return (ValueOperations) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class[]{ValueOperations.class}, new RedisOperationsHandler(delegate));
    }
}
