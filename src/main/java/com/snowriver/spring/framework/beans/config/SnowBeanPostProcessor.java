package com.snowriver.spring.framework.beans.config;

public class SnowBeanPostProcessor {
    //为在 Bean 的初始化前提供回调入口
    public Object postProcessBeforeInitialization(Object instance, String beanName) {
        return instance;
    }

    //为在 Bean 的初始化之后提供回调入口
    public Object postProcessAfterInitialization(Object instance, String beanName) {
        return instance;
    }
}