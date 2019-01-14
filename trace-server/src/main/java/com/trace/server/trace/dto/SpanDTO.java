package com.trace.server.trace.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jianlong_li
 * @date 2018/11/12 19:23
 */
@Data
public class SpanDTO implements Serializable {
    private static final long serialVersionUID = 6971965980267843589L;

    private String traceId;
    private String parentId;
    private String id;

    private String name;
    private String traceType;
    private String resultType;

    private String localServiceName;
    private String localHost;
    private Integer localPort;

    private String remoteServiceName;
    private String remoteHost;
    private Integer remotePort;

    private Long start;
    private String startTime;
    private Long duration;
    private String exceptionType;
    private String exceptionMsg;
    private Map<String, String> tagMap;
}
