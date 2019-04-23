package com.snowriver.spring.framework.aop.support;

import com.snowriver.spring.framework.aop.aspect.SnowAfterReturningAdvice;
import com.snowriver.spring.framework.aop.aspect.SnowAfterThrowingAdvice;
import com.snowriver.spring.framework.aop.config.SnowAopConfig;
import com.snowriver.spring.framework.aop.aspect.SnowMethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnowAdvisedSupport {

    private Class targetClass;
    private Object target;
    private Pattern pointCutClassPattern;
    private SnowAopConfig config;

    private transient Map<Method, List<Object>> methodCache;

    public SnowAdvisedSupport(SnowAopConfig config) {
        this.config = config;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<Object> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);

        // 缓存未命中，进行下一步处理
        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);
            // 底层逻辑，对代理方法进行一个兼容处理
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    public boolean pointCutMatch(){
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    private void parse() {
        methodCache = new HashMap<>();

        // pointCut表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        String pointCutForClass = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);

        pointCutClassPattern = Pattern.compile("class " + pointCutForClass.substring(pointCutForClass.lastIndexOf(" ") + 1));
        Pattern pattern = Pattern.compile(pointCut);

        try {
            Class<?> aspectClass = Class.forName(config.getAspectClass());
            Map<String, Method> sapectMethods = new HashMap<>();

            for (Method method : aspectClass.getMethods()) {
                sapectMethods.put(method.getName(), method);
            }

            // 得到得方法是原生的方法
            for (Method m : targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    // 能满足切面规则的类， 添加到Aop配置中
                    List<Object> advices = new LinkedList<>();
                    // 前置通知
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore().trim()))) {
                        advices.add(new SnowMethodBeforeAdvice(sapectMethods.get(config.getAspectBefore()), aspectClass.newInstance()));
                    }
                    // 后置通知
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter().trim()))) {
                        advices.add(new SnowAfterReturningAdvice(sapectMethods.get(config.getAspectAfter()), aspectClass.newInstance()));
                    }
                    // 异常通知
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow().trim()))) {
                        SnowAfterThrowingAdvice afterThrowingAdvice = new SnowAfterThrowingAdvice(sapectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        afterThrowingAdvice.setThrowingName(config.getAspectAfterThrowingName());
                        advices.add(afterThrowingAdvice);
                    }

                    methodCache.put(m, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}