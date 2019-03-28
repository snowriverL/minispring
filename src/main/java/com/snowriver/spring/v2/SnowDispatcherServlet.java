package com.snowriver.spring.v2;

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
import java.util.*;

/**
 * 郭富豪的Spring实现
 */
public class SnowDispatcherServlet extends HttpServlet {

    //保存 application.properties 配置文件中的内容
    private Properties contextConfig = new Properties();
    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    //保存 url 和 Method 的对应关系
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


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
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().print("404NotFound");
            return;
        }

        Method method = this.handlerMapping.get(url);
        Map<String, String[]> parameterMap = req.getParameterMap();
        method.invoke(this.ioc.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName())), new Object[]{req, resp, parameterMap.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3、 初始化扫描到的类， 并且将它们放入到 ICO 容器之中
        doInstance();
        //4、 完成依赖注入
        doAutowired();
        //5、 初始化 HandlerMapping
        initHandlerMapping();
        System.out.println("GP Spring framework is init.");

    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(SnowController.class)){continue;}

            String baseUrl = "";
            if (clazz.isAnnotationPresent(SnowRequestMapping.class)) {
                baseUrl = clazz.getAnnotation(SnowRequestMapping.class).value();
            }

            //获取所有得public得方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(SnowRequestMapping.class)){continue;}

                SnowRequestMapping annotation = method.getAnnotation(SnowRequestMapping.class);

                String url = ("/" + baseUrl + "/" + annotation.value()).replaceAll("/+", "/");

                handlerMapping.put(url, method);
                System.out.println(url + "========================" + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry: ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(SnowAutowired.class)) {
                    continue;
                }
                SnowAutowired annotation = field.getAnnotation(SnowAutowired.class);
                String beanName = annotation.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(SnowController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(SnowService.class)) {
                    SnowService annotation = clazz.getAnnotation(SnowService.class);
                    String beanName = annotation.value();

                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The “" + i.getName() + "” is exists!!");
                        }

                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //之所以加， 是因为大小写字母的 ASCII 码相差 32，
        // 而且大写字母的 ASCII 码要小于小写字母的 ASCII 码
        //在 Java 中， 对 char 做算学运算， 实际上就是对 ASCII 码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doLoadConfig(String contextConfigLocation) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            //加载配置文件
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                classNames.add(clazzName);
            }
        }
    }
}