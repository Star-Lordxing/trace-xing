package com.sscf.games.lib.trace.common.em;

import lombok.Getter;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Getter
public enum TraceResultTypeEnum {
    SUCCESS("success", 1),
    ERROR_RESULT("error_result", 2),
    EXCEPTION("exception", 3),
    TIMEOUT("timeout", 4);

    private String name;

    /**
     * 优先级，当存在两个结果类型时，优先取值大的
     */
    private int priority;

    TraceResultTypeEnum(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public static TraceResultTypeEnum maxPriority(TraceResultTypeEnum typeEnum1, TraceResultTypeEnum typeEnum2) {
        if (typeEnum1 == null) {
            return typeEnum2;
        }
        if (typeEnum2 == null) {
            return typeEnum1;
        }
        return typeEnum1.getPriority() > typeEnum2.getPriority() ? typeEnum1 : typeEnum2;
    }
}
