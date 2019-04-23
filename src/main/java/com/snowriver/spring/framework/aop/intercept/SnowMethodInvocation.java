package com.snowriver.spring.framework.aop.intercept;

import com.snowriver.spring.framework.aop.aspect.SnowJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnowMethodInvocation implements SnowJoinPoint {

    private Object proxy;
    private Method method;
    private Object target;
    private Class<?> targetClass;
    private Object[] arguments;
    private List<Object> interceptorsAndDynamicMethodMatchers;

    private Map<String, Object> userAttributes;

    private int currentInterceptorIndex = -1;

    public SnowMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<Object> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable{
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++currentInterceptorIndex);

        if (interceptorOrInterceptionAdvice instanceof SnowMethodInterceptor) {
            SnowMethodInterceptor mi = (SnowMethodInterceptor)interceptorOrInterceptionAdvice;
            return mi.invoke(this);
        } else {
            return proceed();
        }
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<String,Object>();
            }
            this.userAttributes.put(key, value);
        }
        else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }

}