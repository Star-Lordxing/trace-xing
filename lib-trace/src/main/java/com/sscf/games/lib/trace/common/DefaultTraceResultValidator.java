package com.sscf.games.lib.trace.common;

import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author pyfight
 * @date 2018/11/18.
 */
@Slf4j
public class DefaultTraceResultValidator implements TraceResultValidator {

    @Override
    public boolean validate(TraceSpan traceSpan, Object result) {
        log.info("validate: --> " + result);

        // 针对httpCode，如果为4XX或者5XX则认为是错误结果
        if (traceSpan.getTraceTypeEnum() == TraceTypeEnum.HTTP_CLIENT ||
                traceSpan.getTraceTypeEnum() == TraceTypeEnum.HTTP_SERVER) {
            String code = traceSpan.getTag(TraceTagConstants.HTTP_RESP_CODE);
            return StringUtils.isEmpty(code) || isSuccessHttpCode(code);
        }
        return true;
    }

    private boolean isSuccessHttpCode(String code) {
        return !code.startsWith("4") && !code.startsWith("5");
    }
}