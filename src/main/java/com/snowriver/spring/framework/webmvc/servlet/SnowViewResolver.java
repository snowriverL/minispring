package com.snowriver.spring.framework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

/**
 * 1.将一个静态文件变为一个动态文件
 * 2.根据用户传送参数不同，产生不同的结果
 * // 最终输出字段，交给Response处理
 */
public class SnowViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFX = ".html";

    private File templateRootDir;
    private String viewName;

    public SnowViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }

    public SnowView resolveViewName(String viewName, Locale locale) throws Exception{
        this.viewName = viewName;
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new SnowView(templateFile);
    }

    public String getViewName() {
        return viewName;
    }
}