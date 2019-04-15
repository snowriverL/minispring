package com.snowriver.spring.framework.beans;

/**
 * Ioc容器，存储Bean得实例
 * DI完成得时候存储到Wrapper
 */
public class SnowBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public SnowBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    /**
     * 返回代理以后得Class
     * 可能会是$Proxy0
     * @return
     */
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}