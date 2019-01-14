package com.trace.server.es.em;

public enum AggTypeEm {

    AVG("avg", "平均"),
    COUNT("count", "数量"),
    SUM("sum", "求和"),
    MIN("mix", "最小值"),
    MAX("max", "最大值"),
    TERMS("terms", "词典");

    private String type;
    private String desc;

    AggTypeEm(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
