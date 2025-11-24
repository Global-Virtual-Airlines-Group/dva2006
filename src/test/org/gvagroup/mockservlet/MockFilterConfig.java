package org.gvagroup.mockservlet;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import java.util.*;

public class MockFilterConfig implements FilterConfig {

    private final String filterName;
    private final ServletContext servletContext;
    private final Map<String, String> initParams = new HashMap<>();

    public MockFilterConfig(String filterName, ServletContext servletContext) {
        this.filterName = filterName;
        this.servletContext = servletContext;
    }

    public void addInitParameter(String name, String value) {
        initParams.put(name, value);
    }

    @Override public String getFilterName() { return filterName; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public String getInitParameter(String name) { return initParams.get(name); }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParams.keySet()); }
}
