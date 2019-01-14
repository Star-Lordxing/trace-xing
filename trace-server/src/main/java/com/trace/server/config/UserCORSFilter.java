package com.trace.server.config;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserCORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String origin = request.getHeader("origin");
        // 给一个带有withCredentials的请求发送响应的时候,服务器端必须指定允许请求的域名,不能使用'*'
        response.setHeader("Access-Control-Allow-Credentials", "true");
        // 表明服务器接受来自指定站点的跨站请求
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Headers", "*");
        response.addHeader("Access-Control-Allow-Methods", "*");
        /*
         * 如果服务器端指定了域名,而不是'*',那么响应头的Vary值里必须包含Origin. 它告诉客户端:
         * 响应是根据请求头里的Origin的值来返回不同的内容的.
         */
        response.setHeader("Vary", "Accept-Encoding,Origin");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
