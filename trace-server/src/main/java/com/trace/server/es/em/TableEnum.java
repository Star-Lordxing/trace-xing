package com.trace.server.es.em;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月14日
 * @since 1.0
 */
public enum TableEnum {
    TABLE_TRACE("trace", "_doc", "id", "调用链");

    private String index;
    private String type;
    private String routing;
    private String desc;

    TableEnum(String index, String type, String routing, String desc) {
        this.index = index;
        this.type = type;
        this.routing = routing;
        this.desc = desc;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getRouting() {
        return routing;
    }

    public String getDesc() {
        return desc;
    }
}
