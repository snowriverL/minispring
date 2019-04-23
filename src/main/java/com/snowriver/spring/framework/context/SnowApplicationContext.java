package com.snowriver.spring.framework.context;

import com.snowriver.spring.framework.annotation.SnowAutowired;
import com.snowriver.spring.framework.annotation.SnowController;
import com.snowriver.spring.framework.annotation.SnowService;
import com.snowriver.spring.framework.aop.SnowAopProxy;
import com.snowriver.spring.framework.aop.SnowCglibAopProxy;
import com.snowriver.spring.framework.aop.SnowJdkDynamicAopProxy;
import com.snowriver.spring.framework.aop.config.SnowAopConfig;
import com.snowriver.spring.framework.aop.support.SnowAdvisedSupport;
import com.snowriver.spring.framework.beans.SnowBeanWrapper;
import com.snowriver.spring.framework.beans.config.SnowBeanDefinition;
import com.snowriver.spring.framework.beans.config.SnowBeanPostProcessor;
import com.snowriver.spring.framework.beans.support.SnowBeanDefinitionReader;
import com.snowriver.spring.framework.context.support.SnowDefaultListableBeanFactory;
import com.snowriver.spring.framework.core.SnowBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SnowApplicationContext extends SnowDefaultListableBeanFactory implements SnowBeanFactory {

    private String[] configLocations;
    private SnowBeanDefinitionReader reader;

    /**
     * 用来保存注册时单例的容器
      */
    private Map<String, Object> singletonBeanCacheMap = new HashMap<>();

    /**
     * 用来存储所有的被代理过的对象
     */
    private Map<String, SnowBeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

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
     *
     *     通过缓存机制来处理循环依赖得问题
     *     如果注入得Bean暂时没有初始化，会打上标记，等Bean初始化完成后，会完成注入操作
     *     通过2个阶段得处理来解决循环依赖
     * }
     * @param beanName
     * @return
     */

    @Override
    public Object getBean(String beanName) {

        SnowBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        try {
            // 生成事件通知
            SnowBeanPostProcessor beanPostProcessor = new SnowBeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);

            if (instance == null) {
                return null;
            }
            // 在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance ,beanName);
            SnowBeanWrapper beanWrapper = new SnowBeanWrapper(instance);
            this.beanWrapperMap.put(beanName, beanWrapper);
            // 在实例初始化以后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            populateBean(beanName, instance);

            // 通过这样一调用，相当于给我们留有了可操作得空间
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过BeanDefinition获取一个实例Bean
     * @param beanDefinition
     * @return
     */
    private Object instantiateBean(SnowBeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();

        try {
            if (this.singletonBeanCacheMap.containsKey(className)) {
                instance = singletonBeanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                // 处理Aop
                SnowAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTarget(instance);
                config.setTargetClass(clazz);

                if (config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }

                this.singletonBeanCacheMap.put(beanDefinition.getFactoryBeanName(), instance);
                this.singletonBeanCacheMap.put(className,instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private SnowAopProxy createProxy(SnowAdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new SnowJdkDynamicAopProxy(config);
        }
        return new SnowCglibAopProxy(config);
    }

    private SnowAdvisedSupport instantionAopConfig(SnowBeanDefinition beanDefinition) {
        SnowAopConfig config = new SnowAopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new SnowAdvisedSupport(config);
    }

    /**
     * 依赖注入
     * @param beanName
     * @param instance
     */
    private void populateBean(String beanName, Object instance) {
        Class<?> clazz = instance.getClass();
        if (!(clazz.isAnnotationPresent(SnowController.class) || clazz.isAnnotationPresent(SnowService.class))) {
            return;
        }

        // 获取所有得属性
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(SnowAutowired.class)) {
                continue;
            }

            SnowAutowired autowired = field.getAnnotation(SnowAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                if(this.beanWrapperMap.get(autowiredBeanName) == null){ continue; }
                // 完成依赖注入
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
        }
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new  String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}