package com.sscf.games.lib.trace.common;

import lombok.Data;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Data
public class ServicePoint {

    private String serviceName;
    private String host;
    private Integer port;

    public ServicePoint() {
    }

    public ServicePoint(String serviceName, String host, Integer port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }
}
