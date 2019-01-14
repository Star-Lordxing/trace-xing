package com.trace.server.es.bean;

import com.trace.server.es.config.ResultConstant;
import lombok.Data;

@Data
public class Result<T> {
    String index;
    String type;
    String id;
    Long version;
    String code;
    String msg;

    double min = 0D;
    double max = 0D;
    double avg = 0D;
    double sum = 0D;
    int count = 0;

    private T data;

    private T data1;

    public Result() {
    }

    public Result(String index, String type, String id, Long version) {
        this.index = index;
        this.type = type;
        this.id = id;
        this.version = version;
    }

    public Result<T> buildSuccess(T t) {
        this.code = ResultConstant.SUCCEED_CODE;
        this.msg = ResultConstant.SUCCEED_MSG;
        this.data = t;
        return this;
    }

    public Result<T> buildSuccess() {
        this.code = ResultConstant.SUCCEED_CODE;
        this.msg = ResultConstant.SUCCEED_MSG;
        return this;
    }

    public Result<T> buildFail(String code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    public boolean isSuccess() {
        return ResultConstant.SUCCEED_CODE.equals(this.code);
    }
}
