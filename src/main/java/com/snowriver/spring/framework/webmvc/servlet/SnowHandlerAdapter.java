package com.snowriver.spring.framework.webmvc.servlet;

import com.snowriver.spring.framework.annotation.SnowRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SnowHandlerAdapter {
    public SnowModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception{
        SnowHandlerMapping handlerMapping = (SnowHandlerMapping)handler;

        // 每一个方法有一个参数列表，这里保存的是形参列表
        Map<String, Integer> paramMapping  = new HashMap<>();

        // 命名参数
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof SnowRequestParam) {
                    String paramName = ((SnowRequestParam) a).value();
                    if (!"".equals(paramName)) {
                        paramMapping.put(paramName, i);
                    }
                }
            }
        }

        // 根据用户请求的参数信息，跟method中的参数信息进行动态匹配
        // resp 传进来的目的只有一个： 只是为了将其赋值给方法参数， 仅此而已

        // 只有用户传过来的modelAndView为空的时候，才会new一个默认的

        // 1.准备好方法的形参列表
        // 方法重载：形参的决定因素：参数的个数，参数的类型，参数顺序，方法的名字
        // 只处理Request和Response
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramMapping.put(type.getName(), i);
            }
        }

        // 2.拿到自定义命名参数所在的位置
        // 用户通过Url传过来的参数列表
        Map<String, String[]> reqParameterMap = req.getParameterMap();

        // 3.构造实参列表
         Object[] paramValues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            if (!paramMapping.containsKey(param.getKey())) {
                continue;
            }

            Integer index = paramMapping.get(param.getKey());

            // 因为页面上传过来的参数都是String类型的，而在方法中定义的类型是千变万化的
            // 要针对传过来的参数进行类型转化
            paramValues[index] = caseStringValue(value, parameterTypes[index]);
        }

        if (paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        // 从handler中取出controller，method，然后利用反射机制调用
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);

        if (result == null) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == SnowModelAndView.class;
        if (isModelAndView) {
            return (SnowModelAndView)result;
        } else {
            return null;
        }

    }

    private Object caseStringValue(String value, Class<?> clazz) {
        if(clazz == String.class){
            return value;
        }else if(clazz == Integer.class){
            return Integer.valueOf(value);
        }else if(clazz == int.class){
            return Integer.valueOf(value).intValue();
        }else {
            return null;
        }
    }

    public boolean support(Object handler) {
        return handler instanceof SnowHandlerMapping;
    }
}