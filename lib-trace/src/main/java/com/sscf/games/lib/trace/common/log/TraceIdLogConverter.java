package com.sscf.games.lib.trace.common.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import com.sscf.games.lib.trace.common.TraceContext;

/**
 * @author jianlong_li
 * @date 2018/11/21 14:10
 */
public class TraceIdLogConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        TraceContext context = TraceContext.getContext();
        if (context == null || context.peekSpan() == null) {
            return CoreConstants.EMPTY_STRING;
        }
        return context.peekSpan().getTraceId();
    }
}
