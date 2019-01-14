package com.sscf.games.lib.trace.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jianlong_li
 * @date 2018/11/16 16:19
 */
public class DefaultHttpHeaderFilter implements HttpHeaderFilter {

    private static final Set<String> FILTER_SET = new HashSet<>();

    static {
        // 请求头参考org.springframework.http.HttpHeaders，
        // "referer","Cookie","user-agent" 以及其他自定义的头不做过滤
        FILTER_SET.addAll(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language",
                "accept-ranges", "access-control-allow-credentials", "access-control-allow-headers",
                "access-control-allow-methods", "access-control-allow-origin", "access-control-expose-headers",
                "access-control-max-age", "access-control-request-headers", "access-control-request-method",
                "age", "allow", "authorization", "cache-control", "connection", "content-encoding", "content-disposition",
                "content-language", "content-length", "content-location", "content-range", "content-type"
                , "date", "etag", "expect", "expires", "from", "host",
                "if-match", "if-modified-since", "if-none-match", "if-range",
                "if-unmodified-since", "last-modified", "link", "location", "max-forwards",
                "origin", "pragma", "proxy-authenticate", "proxy-authorization", "range",
                "retry-after", "server", "te", "trailer", "transfer-encoding",
                "upgrade", "vary", "via", "warning", "www-authenticate"));
    }

    @Override
    public Map<String, String> filter(Map<String, String> originHeaderMap) {
        return originHeaderMap.entrySet().parallelStream()
                .filter(e -> e.getKey() != null && !FILTER_SET.contains(e.getKey().toLowerCase()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
