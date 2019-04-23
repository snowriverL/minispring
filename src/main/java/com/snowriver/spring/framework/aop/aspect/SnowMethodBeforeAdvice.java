package com.snowriver.spring.framework.aop.aspect;

import com.snowriver.spring.framework.aop.intercept.SnowMethodInterceptor;
import com.snowriver.spring.framework.aop.intercept.SnowMethodInvocation;

import java.lang.reflect.Method;

public class SnowMethodBeforeAdvice extends SnowAbstractAspectJAdvice implements SnowAdvice, SnowMethodInterceptor {

    private SnowJoinPoint joinPoint;

    public SnowMethodBeforeAdvice(Method aspectMethod, Object tartget) {
        super(aspectMethod, tartget);
    }

    public void before(Method method, Object[] args, Object target) throws Throwable {
        invokeAdviceMethod(this.joinPoint,null,null);
    }

    @Override
    public Object invoke(SnowMethodInvocation mi) throws Throwable {
        this.joinPoint = mi;
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}