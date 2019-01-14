package com.trace.server.es.bean;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    private Integer pageNo;
    private Integer pageSize;
    private int totalCount;
    private List<T> list;

    public Page(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo == null ? 0 : pageNo - 1;
        this.pageSize = pageSize == null ? 200 : pageSize;
    }

    public Page() {
    }
}
