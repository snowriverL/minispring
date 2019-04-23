package com.snowriver.spring.framework.aop.intercept;

public interface SnowMethodInterceptor {
    Object invoke(SnowMethodInvocation mi) throws Throwable;
}
