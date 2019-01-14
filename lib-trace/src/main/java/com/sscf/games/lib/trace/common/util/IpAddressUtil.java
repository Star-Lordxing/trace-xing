package com.sscf.games.lib.trace.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class IpAddressUtil {
    private IpAddressUtil() {
    }

    /**
     * 转发过来的真实IP，经过多级代理，这里会有多个IP用逗号分隔
     */
    public static String getRequestIp(HttpServletRequest request) {
        String ip = StringUtils.trim(request.getHeader("X-Forwarded-For"));
        if (StringUtils.isNotBlank(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP才是真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

	/*
    public static String getRequestIp(HttpServletRequest request) {
		String custip = request.getHeader("x-forwarded-for");
		if (custip == null || custip.length() == 0 || "unknown".equalsIgnoreCase(custip)) {
			custip = request.getHeader("Proxy-Client-IP");
		}
		if (custip == null || custip.length() == 0 || "unknown".equalsIgnoreCase(custip)) {
			custip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (custip == null || custip.length() == 0 || "unknown".equalsIgnoreCase(custip)) {
			custip = request.getRemoteAddr();
		}
		return custip;
	}
	*/
}
