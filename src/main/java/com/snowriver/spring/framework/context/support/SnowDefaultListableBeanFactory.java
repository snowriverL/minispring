package com.snowriver.spring.framework.context.support;

import com.snowriver.spring.framework.beans.config.SnowBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SnowDefaultListableBeanFactory extends SnowAbstractApplicationContext{

    protected final Map<String, SnowBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

}