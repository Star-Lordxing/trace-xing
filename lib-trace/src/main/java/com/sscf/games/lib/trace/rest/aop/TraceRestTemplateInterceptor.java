package com.sscf.games.lib.trace.rest.aop;

import com.sscf.games.lib.trace.common.ServicePoint;
import com.sscf.games.lib.trace.common.TraceContext;
import com.sscf.games.lib.trace.common.TraceHeader;
import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 对RestTemplate的拦截{@link RestTemplateAspect} 进行traceSpan进行补充（code、header、method等）
 *
 * @author jianlong_li
 * @date 2018/11/11.
 */
@Slf4j

public class TraceRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        TraceContext traceContext = TraceContext.getContext();

        TraceSpan traceSpan = traceContext.peekSpan(TraceTypeEnum.HTTP_CLIENT);
        if (traceSpan == null) {
            log.error("存在未拦截的HTTP请求：req={}", request.getURI());
            return execution.execute(request, body);
        }

        URI requestURI = request.getURI();
        traceSpan.setName(this.filterParamUrl(requestURI.toString()));
        traceSpan.setRemotePoint(new ServicePoint(null, requestURI.getHost(), requestURI.getPort()));
        Map<String, String> reqHeaderMap = this.toHeaderMap(request.getHeaders());
        traceSpan.addTag(TraceTagConstants.HTTP_REQ_HEADER, Objects.toString(traceContext.filterHttpHeader(reqHeaderMap)));
        traceSpan.addTag(TraceTagConstants.HTTP_REQ_METHOD, request.getMethod() == null ? "" : request.getMethod().name());
        if (request.getURI().getScheme().equalsIgnoreCase("https")) {
            traceSpan.getLocalPoint().setPort(TraceContext.HTTPS_PORT);
        }
        // 加入Trace请求头
        Map<String, String> traceHeaderMap = TraceHeader.newByTraceSpan(traceSpan).toMap();
        for (Map.Entry<String, String> header : traceHeaderMap.entrySet()) {
            request.getHeaders().add(header.getKey(), header.getValue());
        }
        // 执行http
        ClientHttpResponse httpResponse = execution.execute(request, body);
        traceSpan.addTag(TraceTagConstants.HTTP_RESP_CODE, String.valueOf(httpResponse.getRawStatusCode()));
        return httpResponse;
    }

    private String filterParamUrl(String url) {
        int index = url.indexOf('?');
        return index >= 0 ? url.substring(0, index) : url;
    }

    private Map<String, String> toHeaderMap(HttpHeaders httpHeaders) {
        return httpHeaders.entrySet().parallelStream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, h -> Objects.toString(h.getValue())));
    }

}
