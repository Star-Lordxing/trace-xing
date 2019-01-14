package com.sscf.games.lib.trace.redis;

import lombok.Data;

/**
 * @author jianlong_li
 * @date 2018/11/14 10:10
 */
@Data
public class RedisServer {
    private String host;
    private Integer port;

    public RedisServer() {
    }

    public RedisServer(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
}
