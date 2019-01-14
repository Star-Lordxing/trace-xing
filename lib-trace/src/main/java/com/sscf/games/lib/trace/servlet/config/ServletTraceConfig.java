package com.sscf.games.lib.trace.servlet.config;

import com.sscf.games.lib.trace.servlet.aop.TraceServletAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jianlong_li
 * @date 2018/11/19 20:57
 */
@ConditionalOnClass(RequestMapping.class)
@Configuration
public class ServletTraceConfig {

    @Bean
    public TraceServletAspect traceServletAspect() {
        return new TraceServletAspect();
    }

}
