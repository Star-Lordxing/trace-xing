package com.sscf.games.lib.trace.common.log;

import ch.qos.logback.classic.PatternLayout;

/**
 * @author jianlong_li
 * @date 2018/11/21 11:30
 */
public class TraceLogLayout extends PatternLayout {
    static {
        defaultConverterMap.put("traceId", TraceIdLogConverter.class.getName());
    }
}
