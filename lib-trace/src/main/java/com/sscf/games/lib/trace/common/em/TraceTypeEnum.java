package com.sscf.games.lib.trace.common.em;

import lombok.Getter;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Getter
public enum TraceTypeEnum {
    /**
     * restTemplate
     */
    HTTP_CLIENT("http-client"),
    /**
     * Controller
     */
    HTTP_SERVER("http-server"),

    /**
     * dubbo提供方，被调用者
     */
    RPC_PRODUCER("rpc-producer"),
    /**
     * dubbo消费方，调用者
     */
    RPC_CONSUMER("rpc-consumer"),

    /**
     * redisTemplate
     */
    REDIS_CLIENT("redis-client");

    private String name;

    TraceTypeEnum(String name) {
        this.name = name;
    }
}
