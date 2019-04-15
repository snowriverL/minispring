package com.snowriver.spring.framework.context.support;

/**
 * IOC容器实现得顶层设计
 */
public abstract class SnowAbstractApplicationContext {

    /**
     * 受保护，只提供给子类重写
     * @throws Exception
     */
    public void refresh() throws Exception{};
}