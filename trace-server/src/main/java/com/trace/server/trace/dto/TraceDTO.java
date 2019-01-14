package com.trace.server.trace.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月17日
 * @since 1.0
 */
@Data
public class TraceDTO {
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

    private List<TraceDTO> Children;
}
