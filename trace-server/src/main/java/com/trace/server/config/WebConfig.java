package com.trace.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author zhangwei
 * @since 17:00 2017/12/27
 */
@Configuration
@Slf4j
public class WebConfig extends WebMvcConfigurerAdapter {
    @Bean
    public FilterRegistrationBean userCorsFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean<>(new UserCORSFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
