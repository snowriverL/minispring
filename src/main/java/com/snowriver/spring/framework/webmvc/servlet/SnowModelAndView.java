package com.snowriver.spring.framework.webmvc.servlet;

import java.util.Map;

public class SnowModelAndView {

    private String viewName;
    private Map<String,?> model;

    public SnowModelAndView(String viewName) {
        this(viewName, null);
    }

    public SnowModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}