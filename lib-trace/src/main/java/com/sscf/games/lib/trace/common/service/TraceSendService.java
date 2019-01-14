package com.sscf.games.lib.trace.common.service;

import com.sscf.games.lib.trace.common.TraceSpan;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
public interface TraceSendService {
    void send(TraceSpan traceSpan);
}