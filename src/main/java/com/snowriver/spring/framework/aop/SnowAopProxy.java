package com.snowriver.spring.framework.aop;

/**
 * 默认使用JDK代理
 */
public interface SnowAopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
