package com.snowriver.spring.framework.aop.aspect;

import com.snowriver.spring.framework.aop.intercept.SnowMethodInterceptor;
import com.snowriver.spring.framework.aop.intercept.SnowMethodInvocation;

import java.lang.reflect.Method;

public class SnowAfterThrowingAdvice extends SnowAbstractAspectJAdvice implements SnowAdvice, SnowMethodInterceptor {

    private String throwingName;
    private SnowMethodInvocation mi;

    public SnowAfterThrowingAdvice(Method aspectMethod, Object target) {
        super(aspectMethod, target);
    }

    public void setThrowingName(String throwingName) {
        this.throwingName = throwingName;
    }

    @Override
    public Object invoke(SnowMethodInvocation mi) throws Throwable{
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            ex.printStackTrace();
            invokeAdviceMethod(mi, null, ex.getCause());
            throw ex;
        }
    }
}