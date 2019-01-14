package com.trace.server.es.bean;

import com.trace.server.es.em.AggTypeEm;
import lombok.Getter;

@Getter
public class AggEntity {

    // 类型
    private AggTypeEm typeEm;

    // 统计的字段名
    private String field;

    // 返回记录数
    private int size;

    public AggEntity(AggTypeEm typeEm, String field) {
        this.typeEm = typeEm;
        this.field = field;
    }

    public AggEntity(AggTypeEm typeEm, String field, int size) {
        this.typeEm = typeEm;
        this.field = field;
        this.size = size;
    }

}
