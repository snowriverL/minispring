package com.snowriver.spring.framework.beans.config;

/**
 * 用来存储配置文件中得信息
 * 相当于保存在内存中得配置
 */
public class SnowBeanDefinition {
    /**
     * bean类名
     */
    private String beanClassName;
    /**
     * 判断是否是懒加载，默认false，refresh完成ioc初始化后，调用getBean()，完成相关类得依赖注入（DI）
     */
    private boolean lazyInit = false;
    /**
     * 工厂Bean
     */
    private String factoryBeanName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}