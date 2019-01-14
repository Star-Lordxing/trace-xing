package com.sscf.games.lib.trace.common;

import java.util.Map;

/**
 * @author jianlong_li
 * @date 2018/11/16 16:19
 */
public interface HttpHeaderFilter {

    /**
     * 过滤Http请求头
     *
     * @param originHeaderMap 原始请求头Map
     * @return 过滤后的请求头Map
     */
    Map<String, String> filter(Map<String, String> originHeaderMap);
}
