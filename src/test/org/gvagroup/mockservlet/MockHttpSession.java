package org.gvagroup.mockservlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.*;

public class MockHttpSession implements HttpSession {

    private final String id = UUID.randomUUID().toString();
    private final Map<String, Object> attributes = new HashMap<>();
    private final long creationTime = System.currentTimeMillis();
    private long lastAccessedTime = creationTime;
    private int maxInactiveInterval = 1800;
    private boolean invalidated = false;
    private ServletContext servletContext;

    public MockHttpSession() {}
    public MockHttpSession(ServletContext context) { this.servletContext = context; }

    @Override public long getCreationTime() { checkValid(); return creationTime; }
    @Override public String getId() { return id; }
    @Override public long getLastAccessedTime() { checkValid(); return lastAccessedTime; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public void setMaxInactiveInterval(int interval) { this.maxInactiveInterval = interval; }
    @Override public int getMaxInactiveInterval() { return maxInactiveInterval; }

    @Override public Object getAttribute(String name) { checkValid(); return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { checkValid(); return Collections.enumeration(attributes.keySet()); }
    @Override public void setAttribute(String name, Object value) { checkValid(); attributes.put(name, value); }
    @Override public void removeAttribute(String name) { checkValid(); attributes.remove(name); }

    @Override public void invalidate() { invalidated = true; attributes.clear(); }
    @Override public boolean isNew() { return false; }

    private void checkValid() {
        if (invalidated) throw new IllegalStateException("Session is invalidated");
    }
}
