package com.snowriver.spring.framework.core;

/**
 * DI注入得触发时机，调用GetBean出发
 */
public interface SnowBeanFactory {

    /**
     * 根据 beanName 从 IOC 容器之中获得一个实例 Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
