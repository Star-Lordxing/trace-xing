package com.sscf.games.lib.trace.redis.config;

import com.sscf.games.lib.trace.redis.RedisServer;
import com.sscf.games.lib.trace.redis.aop.RedisTemplateAspect;
import com.sscf.games.lib.trace.redis.util.RedisAopUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author jianlong_li
 * @date 2018/11/13 11:14
 */
@ConditionalOnClass(RedisTemplate.class)
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisTraceConfig {

    private static final RedisServer REDIS_SERVER = new RedisServer();

    public RedisTraceConfig(RedisProperties redisProperties) {
        REDIS_SERVER.setHost(redisProperties.getHost());
        REDIS_SERVER.setPort(redisProperties.getPort());
    }

    @Bean
    public RedisTemplateAspect redisTemplateAspect() {
        return new RedisTemplateAspect();
    }

    @Autowired(required = false)
    public void setRedisTemplate(List<RedisTemplate> redisTemplateList) {
        if (CollectionUtils.isEmpty(redisTemplateList)) {
            return;
        }
        for (RedisTemplate redisTemplate : redisTemplateList) {
            RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
            RedisConnectionFactory redisConnectionFactoryProxy = RedisAopUtil.newRedisConnectionFactoryProxy(connectionFactory);
            redisTemplate.setConnectionFactory(redisConnectionFactoryProxy);
        }
    }

    public static RedisServer getRedisServer() {
        return REDIS_SERVER;
    }
}