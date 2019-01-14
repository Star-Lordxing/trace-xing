package com.sscf.games.lib.trace.common.config;

import com.sscf.games.lib.trace.common.*;
import com.sscf.games.lib.trace.common.service.KafkaTraceSendServiceImpl;
import com.sscf.games.lib.trace.common.service.TraceSendService;
import com.sscf.games.lib.trace.servlet.aop.TraceServletAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CommonConfig {

    @Bean
    public TraceSendService traceSendService() {
        return new KafkaTraceSendServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceResultValidator traceResultValidator() {
        return new DefaultTraceResultValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpHeaderFilter httpHeaderFilter() {
        return new DefaultHttpHeaderFilter();
    }

    @Bean
    public TraceContext traceContext() {
        return new TraceContext();
    }

    @Bean
    public TraceServletAspect traceServletAspect() {
        return new TraceServletAspect();
    }

}
