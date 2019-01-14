package com.sscf.games.lib.trace.common.util;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * 对代理类相关操作
 *
 * @author jianlong_li
 * @date 2017/9/9 14:45
 */
public class ProxyUtil {

    private ProxyUtil() {
    }

    /**
     * 获取 代理最原始的目标对象
     *
     * @param proxy 代理对象
     */
    public static Object getTarget(Object proxy) throws Exception {
        Object targetObj = proxy;
        while (targetObj != null) {
            // spring的动态Proxy
            if (AopUtils.isJdkDynamicProxy(targetObj)) {
                targetObj = getSpringProxyTarget(targetObj);
            }
            // cglib
            else if (AopUtils.isCglibProxy(targetObj)) {
                targetObj = getCglibProxyTarget(targetObj);
            }
            // 原始的jdk动态代理
            else if (Proxy.isProxyClass(targetObj.getClass())) {
                targetObj = getJdkProxyTarget(targetObj);
            } else {
                break;
            }
        }
        return targetObj;
    }

    /**
     * 获取Cglib代理的目标对象（一层）
     */
    private static Object getCglibProxyTarget(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
    }

    /**
     * 获取spring动态代理的目标对象（一层）
     */
    private static Object getSpringProxyTarget(Object proxy) throws Exception {
        Field h = ReflectionUtils.findField(proxy.getClass().getSuperclass(), "h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) ReflectionUtils.getField(h, proxy);
        Field advised = ReflectionUtils.findField(aopProxy.getClass(), "target");
        advised.setAccessible(true);
        return ((AdvisedSupport) ReflectionUtils.getField(advised, aopProxy)).getTargetSource().getTarget();
    }

    /**
     * 获取spring动态代理的目标对象（一层）
     */
    private static Object getJdkProxyTarget(Object proxy) {
        Field h = ReflectionUtils.findField(proxy.getClass().getSuperclass(), "h");
        h.setAccessible(true);
        Object hObject = ReflectionUtils.getField(h, proxy);
        Field targetField;
        try {
            targetField = ReflectionUtils.findField(hObject.getClass(), "target");
        } catch (Exception e) {
            targetField = ReflectionUtils.findField(hObject.getClass(), "delegate");
        }
        targetField.setAccessible(true);
        return ReflectionUtils.getField(targetField, hObject);
    }
}

