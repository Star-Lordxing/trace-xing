package com.sscf.games.lib.trace.common;

import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;

/**
 * @author pyfight
 * @date 2018/11/18.
 */
public interface TraceResultValidator {
    /**
     * 对调用成功的结果进行二次校验
     *
     * @param traceSpan 调用Span
     * @param result    调用的结果(返回值)
     * @return true表示校验通过, 标记结果为：SUCCESS；false则会被标志为RESULT_ERROR
     * @see TraceResultTypeEnum
     */
    boolean validate(TraceSpan traceSpan, Object result);
}