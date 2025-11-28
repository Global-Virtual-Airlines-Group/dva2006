package org.gvagroup.mockservlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.*;

public class MockServletConfig implements ServletConfig {

    private final String servletName;
    private final ServletContext servletContext;
    private final Map<String, String> initParams = new HashMap<>();

    public MockServletConfig(String servletName, ServletContext servletContext) {
        this.servletName = servletName;
        this.servletContext = servletContext;
    }

    public void addInitParameter(String name, String value) { initParams.put(name, value); }

    @Override public String getServletName() { return servletName; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public String getInitParameter(String name) { return initParams.get(name); }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParams.keySet()); }
}
