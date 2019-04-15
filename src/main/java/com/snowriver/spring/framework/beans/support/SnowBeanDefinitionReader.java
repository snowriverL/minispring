package com.snowriver.spring.framework.beans.support;

import com.snowriver.spring.framework.beans.config.SnowBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 对配置文件进行查找，读取，解析
 */
public class SnowBeanDefinitionReader {

    private List<String> registyBeanClasses = new ArrayList<>();

    private Properties config = new Properties();

    private final String SCAN_PACKAGE = "scanPackage";

    public SnowBeanDefinitionReader(String... locations) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doSacnner(config.getProperty(SCAN_PACKAGE));
    }

    private void doSacnner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doSacnner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                registyBeanClasses.add(className);
            }
        }
    }

    public Properties getConfig() {
        return this.config;
    }

    public List<SnowBeanDefinition> loadBeanDefinitions() {
        List<SnowBeanDefinition> result = new ArrayList<>();
        try {
            for (String className : registyBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) {
                    continue;
                }
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> clazz : interfaces) {
                    result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 把每一个配置信息转化为一个BeanDefinition
    private SnowBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        SnowBeanDefinition beanDefinition = new SnowBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //之所以加， 是因为大小写字母的 ASCII 码相差 32，
        // 而且大写字母的 ASCII 码要小于小写字母的 ASCII 码
        //在 Java 中， 对 char 做算学运算， 实际上就是对 ASCII 码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }
}