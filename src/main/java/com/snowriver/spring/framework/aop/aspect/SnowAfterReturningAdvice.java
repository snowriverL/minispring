package com.snowriver.spring.framework.aop.aspect;

import com.snowriver.spring.framework.aop.intercept.SnowMethodInterceptor;
import com.snowriver.spring.framework.aop.intercept.SnowMethodInvocation;

import java.lang.reflect.Method;

public class SnowAfterReturningAdvice extends SnowAbstractAspectJAdvice implements SnowAdvice, SnowMethodInterceptor {
    private SnowJoinPoint joinPoint;

    public SnowAfterReturningAdvice(Method aspectMethod, Object tartget) {
        super(aspectMethod, tartget);
    }

    @Override
    public Object invoke(SnowMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    public void afterReturning(Object returnValue, Method method, Object[] args,Object target) throws
            Throwable{
        invokeAdviceMethod(joinPoint,returnValue,null);
    }
}