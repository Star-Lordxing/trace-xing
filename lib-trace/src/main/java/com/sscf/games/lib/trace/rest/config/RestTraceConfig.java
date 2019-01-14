package com.sscf.games.lib.trace.rest.config;

import com.sscf.games.lib.trace.rest.aop.RestTemplateAspect;
import com.sscf.games.lib.trace.rest.aop.TraceRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author jianlong_li
 * @date 2018/11/13 11:14
 */
@ConditionalOnClass(RestTemplate.class)
@Configuration
public class RestTraceConfig {

    @Bean
    public RestTemplateAspect restTemplateAspect() {
        return new RestTemplateAspect();
    }

    @Autowired(required = false)
    public void setRestTemplateList(List<RestTemplate> restTemplateList) {
        if (CollectionUtils.isEmpty(restTemplateList)) {
            return;
        }
        for (RestTemplate restTemplate : restTemplateList) {
            List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
            interceptors.add(new TraceRestTemplateInterceptor());
            restTemplate.setInterceptors(interceptors);
        }
    }
}
