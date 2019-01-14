package com.sscf.games.lib.trace.common.exception;

/**
 * @author jianlong_li
 * @date 2018/11/19 12:13
 */
public class JsonConvertException extends RuntimeException {
    public JsonConvertException() {
    }

    public JsonConvertException(Throwable cause) {
        super(cause);
    }

    public JsonConvertException(String message) {
        super(message);
    }

    public JsonConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
