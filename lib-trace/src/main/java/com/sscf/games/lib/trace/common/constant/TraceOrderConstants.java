package com.sscf.games.lib.trace.common.constant;

/**
 * 拦截的order
 *
 * @author jianlong_li
 * @date 2018/11/17 15:52
 */
public class TraceOrderConstants {
    private TraceOrderConstants() {
    }

    /**
     * restTemplate拦截的顺序
     */
    public static final int REST_AOP_ORDER = -1;

    /**
     * dubbo的过滤顺序
     */
    public static final int DUBBO_FILTER_ORDER = -20000;

    /**
     * redisTemplate本身方法的拦截顺序，对于OpsValue、opsList 等的操作使用jdk代理
     */
    public static final int REDIS_AOP_ORDER = -1;

    /**
     * servlet的拦截顺序
     */
    public static final int SERVLET_AOP_ORDER = -1;
}
