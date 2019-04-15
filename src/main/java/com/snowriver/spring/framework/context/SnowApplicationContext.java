package com.snowriver.spring.framework.context;

import com.snowriver.spring.framework.beans.config.SnowBeanDefinition;
import com.snowriver.spring.framework.beans.support.SnowBeanDefinitionReader;
import com.snowriver.spring.framework.context.support.SnowDefaultListableBeanFactory;
import com.snowriver.spring.framework.core.SnowBeanFactory;

import java.util.List;
import java.util.Map;

public class SnowApplicationContext extends SnowDefaultListableBeanFactory implements SnowBeanFactory {

    private String[] configLocations;
    private SnowBeanDefinitionReader reader;

    public SnowApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IOC初始化入口
     * @throws Exception
     */
    @Override
    public void refresh() throws Exception {
        // 定位，定位配置文件
        reader = new SnowBeanDefinitionReader(this.configLocations);
        // 加载配置文件，扫描相关得类，封装成BeanDefinition
        List<SnowBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 注册，把配置信息放到容器里面（伪IOC容器）
        doRegisterBeanDifinitions(beanDefinitions);
        // 把不是延时加载得类，提前初始化
        doAutowrited();
    }

    private void doRegisterBeanDifinitions(List<SnowBeanDefinition> beanDefinitions) throws Exception{
        for (SnowBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        // 容器初始化完毕
    }

    /**
     * 只处理非延迟加载得情况
     */
    private void doAutowrited() {
        for (Map.Entry<String, SnowBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }
    }

    /**
     * @see {
     *     依赖注入，从这里开始，听过读取BeanDefinition中得信息
     *     然后，通过反射机制创建一个实例并返回
     *     Spring不会把最原始得对象返回，会用一个BeanWrapper进行一次包装
     *     装饰者模式：
     *     1.保存了原来得OOP关系
     *     2.我需要对它进行扩展，增强（为了以后得Aop打基础）
     * }
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        return null;
    }
}