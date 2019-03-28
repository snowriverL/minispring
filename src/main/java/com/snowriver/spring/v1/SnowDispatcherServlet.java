package com.snowriver.spring.v1;

import com.snowriver.spring.annotation.SnowAutowired;
import com.snowriver.spring.annotation.SnowController;
import com.snowriver.spring.annotation.SnowRequestMapping;
import com.snowriver.spring.annotation.SnowService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 郭富豪的Spring实现
 */
public class SnowDispatcherServlet extends HttpServlet {

    private Map<String, Object> mapping = new HashMap<>();
    private Map<String, Object> requestMapping = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            resp.getWriter().print("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.requestMapping.containsKey(url)) {
            resp.getWriter().print("404NotFound");
            return;
        }

        Method method = (Method) this.requestMapping.get(url);
        Map<String, String[]> parameterMap = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, parameterMap.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            //读取配置文件信息
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            //加载配置文件
            configContext.load(is);
            //读取扫描路径
            String scanPackage = configContext.getProperty("scanPackage");
            //扫描
            doScanner(scanPackage);

            for (String className : mapping.keySet()) {
                if (!className.contains(".")) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(SnowController.class)) {
                    mapping.put(className, clazz.newInstance());
                    //获取请求路径
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(SnowRequestMapping.class)) {
                        SnowRequestMapping annotation = clazz.getAnnotation(SnowRequestMapping.class);
                        baseUrl = annotation.value();
                    }
                    //获取所有得方法
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(SnowRequestMapping.class)) {
                            SnowRequestMapping annotation = method.getAnnotation(SnowRequestMapping.class);
                            //把多个/替换为一个/
                            String url = (baseUrl + annotation.value()).replaceAll("/+", "/");
                            requestMapping.put(url, method);
                            System.out.println("Mapped " + url + "," + method);
                        }
                    }
                } else if (clazz.isAnnotationPresent(SnowService.class)) {
                    SnowService annotation = clazz.getAnnotation(SnowService.class);
                    String beanName = annotation.value();

                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }

                    mapping.put(beanName, clazz.newInstance());

                    for (Class<?> t : clazz.getInterfaces()) {
                        mapping.put(t.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }

            }

            for (Object object : mapping.values()) {
                if (null == object) {
                    continue;
                }
                Class<?> clazz = object.getClass();
                if (!clazz.isAnnotationPresent(SnowController.class)) {
                    continue;
                }
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!field.isAnnotationPresent(SnowAutowired.class)) {
                        continue;
                    }
                    SnowAutowired annotation = field.getAnnotation(SnowAutowired.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    field.setAccessible(true);
                    try {
                        field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (Exception e) {
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

        System.out.println("Snowriver Dispatcher init");

    }

    //扫描所有class加载到mappings
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }

                String clazzName = scanPackage + "." + file.getName().replace(".class", "");
                mapping.put(clazzName, null);
            }
        }
    }
}