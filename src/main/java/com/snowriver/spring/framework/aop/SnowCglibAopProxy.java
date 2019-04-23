package com.snowriver.spring.framework.aop;

import com.snowriver.spring.framework.aop.support.SnowAdvisedSupport;

/**
 * CGlib代理
 */
public class SnowCglibAopProxy implements SnowAopProxy {

    private SnowAdvisedSupport config;

    public SnowCglibAopProxy(SnowAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}