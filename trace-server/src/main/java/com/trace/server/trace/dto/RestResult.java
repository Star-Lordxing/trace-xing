package com.trace.server.trace.dto;

import com.trace.server.es.config.ResultConstant;
import lombok.Data;

@Data
public class RestResult<T> {
    private String code;
    private String msg;
    private T data;

    private T data1;

    public RestResult<T> buildSuccess() {
        this.code = ResultConstant.SUCCEED_CODE;
        this.msg = ResultConstant.SUCCEED_MSG;
        return this;
    }

    public RestResult<T> buildSuccess(T t) {
        this.code = ResultConstant.SUCCEED_CODE;
        this.msg = ResultConstant.SUCCEED_MSG;
        this.data = t;
        return this;
    }

    public RestResult<T> buildSuccess(T t, T t1) {
        this.code = ResultConstant.SUCCEED_CODE;
        this.msg = ResultConstant.SUCCEED_MSG;
        this.data = t;
        this.data1 = t1;
        return this;
    }


    public RestResult<T> buildFail(String code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    public boolean isSuccess(RestResult result) {
        return ResultConstant.SUCCEED_CODE.equals(result.code);
    }
}
