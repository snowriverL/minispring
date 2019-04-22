package com.snowriver.spring.framework.webmvc.servlet;

import com.snowriver.spring.framework.annotation.SnowController;
import com.snowriver.spring.framework.annotation.SnowRequestMapping;
import com.snowriver.spring.framework.context.SnowApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MVC得启动入口
 */
@Slf4j
public class SnowDispatcherServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    private List<SnowHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<SnowHandlerMapping, SnowHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<SnowViewResolver> viewResolvers = new ArrayList<>();

    private SnowApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化Ioc容器
        context = new SnowApplicationContext(config.getInitParameter(LOCATION));
        // 初始化MVC9大组件
        initStrategies(context);
    }

    protected void initStrategies(SnowApplicationContext context) {
        //有九种策略
        // 针对于每个用户请求， 都会经过一些处理的策略之后， 最终才能有结果输出
        // 每种策略可以自定义干预， 但是最终的结果都是一致

        //多文件上传的组件，如果请求类型是multipart,将通过MultipartResolver进行文件上传解析
        initMultipartResolver(context);
        //初始化本地语言环境，本地化解析
        initLocaleResolver(context);
        //初始化模板处理器，主题解析
        initThemeResolver(context);
        //初始化mvc容器，SnowHandlerMapping 用来保存 Controller 中配置的 RequestMapping 和 Method 的一个对应关系,
        //通过HandlerMapping将请求映射到处理器
        initHandlerMappings(context);
        //初始化参数适配器，动态匹配Method参数，包括类转换，动态赋值
        //通过HandlerAdapater进行多类型的参数动态匹配
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化试图预处理器
        initRequestToViewNameTranslator(context);
        //初始化试图转换器，实现动态模板的解析，自己解析模板语言
        //通过ViewResolver解析逻辑视图到具体视图实现
        initViewResolvers(context);
        // FLASH映射管理器
        initFlashMapManager(context);
    }

    /**
     * 将Controller中配置的RequestMapping和Method进行一一对应
     * @param context
     */
    private void initHandlerMappings(SnowApplicationContext context) {
        // 从容器中获取所有的实例
        String[] beanNames = context.getBeanDefinitionNames();

        try {
            for (String beanName : beanNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(SnowController.class)) {
                    continue;
                }

                String baseUrl = "";
                if (clazz.isAnnotationPresent(SnowRequestMapping.class)) {
                    SnowRequestMapping requestMapping = clazz.getAnnotation(SnowRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                // 扫描所有的public方法
                Method[] methods = clazz.getMethods();

                for (Method method : methods) {
                    if (!method.isAnnotationPresent(SnowRequestMapping.class)) {
                        continue;
                    }

                    SnowRequestMapping requestMapping = method.getAnnotation(SnowRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new SnowHandlerMapping(pattern, controller, method));
                    log.info("Mapping:" + regex + " , " + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHandlerAdapters(SnowApplicationContext context) {
        // 在初始化阶段，将这些参数的名字或者类型按一定的顺序保存下来
        // 因为后面用反射调用的时候，传的形参是一个数组
        // 可以通过记录这些参数的index，挨个从数组中填值，这样的话，就和参数的顺序无关了
        for (SnowHandlerMapping handlerMapping : this.handlerMappings) {
            //每一个方法有一个参数列表，这里保存的是形参列表
            this.handlerAdapters.put(handlerMapping, new SnowHandlerAdapter());
        }
    }

    private void initViewResolvers(SnowApplicationContext context) {
        // 解决页面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String tempalteRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File tempalteRootDir = new File(tempalteRootPath);

        for (File template : tempalteRootDir.listFiles()) {
            this.viewResolvers.add(new SnowViewResolver(templateRoot));
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("<font size='25' color='blue'>500Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") + "<fontcolor='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        // 根据用户请求的Url获取一个Handler
        SnowHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new SnowModelAndView("404"));
            return;
        }

        SnowHandlerAdapter ha = getHandlerAdapter(handler);

        // 调用方法，得到返回值
        SnowModelAndView mv =  ha.handle(req, resp, handler);
        // 输出到页面
        processDispatchResult(req, resp, mv);
    }

    private SnowHandlerAdapter getHandlerAdapter(SnowHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        SnowHandlerAdapter ha = this.handlerAdapters.get(handler);
        if (ha.support(handler)) {
            return ha;
        }
        return null;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, SnowModelAndView mv) throws Exception{
        // 调用viewResolver的resolverView方法
        if (mv == null) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        if (this.viewResolvers != null) {
            for (SnowViewResolver resolver : viewResolvers) {
                SnowView view = resolver.resolveViewName(mv.getViewName(), null);
                if (view != null) {
                    view.render(mv.getModel(), req, resp);
                    return;
                }
            }

        }
    }

    private SnowHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (SnowHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }

        return null;
    }

    private void initFlashMapManager(SnowApplicationContext context) {

    }

    private void initRequestToViewNameTranslator(SnowApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(SnowApplicationContext context) {

    }

    private void initThemeResolver(SnowApplicationContext context) {

    }

    private void initLocaleResolver(SnowApplicationContext context) {
    }

    private void initMultipartResolver(SnowApplicationContext context) {
    }
}