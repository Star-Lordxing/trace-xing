package com.trace.server.trace.dto.resp;

import lombok.Data;

@Data
public class MetricsRespDTO {
    private double avgDuration;
    private double maxDuration;
    private double minDuration;
    private int totalCount;
    private int errorCount;
    private String errorRate;
}
