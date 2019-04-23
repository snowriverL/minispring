package com.snowriver.spring.framework.aop;

import com.snowriver.spring.framework.aop.intercept.SnowMethodInvocation;
import com.snowriver.spring.framework.aop.support.SnowAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class SnowJdkDynamicAopProxy implements SnowAopProxy, InvocationHandler {

    private SnowAdvisedSupport config;

    public SnowJdkDynamicAopProxy(SnowAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        try {
            return Proxy.newProxyInstance(classLoader, this.config.getTargetClass().getInterfaces(), this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = config.getInterceptorsAndDynamicInterceptionAdvice(method,this.config.getTargetClass());

        SnowMethodInvocation invocation = new SnowMethodInvocation(proxy, this.config.getTarget(), method, args, this.config.getTargetClass(), interceptorsAndDynamicMethodMatchers);

        return invocation.proceed();
    }
}