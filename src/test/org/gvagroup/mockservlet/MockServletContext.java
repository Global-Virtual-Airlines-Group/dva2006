package org.gvagroup.mockservlet;

import jakarta.servlet.*;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Complete MockServletContext for Servlet 6.0, including character encoding and session timeout.
 */
public class MockServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String> initParams = new HashMap<>();
    private int sessionTimeout = 30; // default 30 mins
    private String requestCharacterEncoding = "UTF-8";
    private String responseCharacterEncoding = "UTF-8";

    // ----------------- Attributes -----------------
    @Override public Object getAttribute(String name) { return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
    @Override public void setAttribute(String name, Object object) { attributes.put(name, object); }
    @Override public void removeAttribute(String name) { attributes.remove(name); }

    // ----------------- Init parameters -----------------
    @Override public String getInitParameter(String name) { return initParams.get(name); }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParams.keySet()); }
    @Override public boolean setInitParameter(String name, String value) {
        if (initParams.containsKey(name)) return false;
        initParams.put(name, value);
        return true;
    }
    public void addInitParameter(String name, String value) { initParams.put(name, value); }

    // ----------------- Context info -----------------
    @Override public String getContextPath() { return ""; }
    @Override public ServletContext getContext(String uripath) { return this; }
    @Override public int getMajorVersion() { return 6; }
    @Override public int getMinorVersion() { return 0; }
    @Override public int getEffectiveMajorVersion() { return 6; }
    @Override public int getEffectiveMinorVersion() { return 0; }
    @Override public String getMimeType(String file) { return null; }
    @Override public Set<String> getResourcePaths(String path) { return Collections.emptySet(); }
    @Override public URL getResource(String path) { return null; }
    @Override public InputStream getResourceAsStream(String path) { return null; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return new MockRequestDispatcher(path); }
    @Override public RequestDispatcher getNamedDispatcher(String name) { return new MockRequestDispatcher(name); }
    @Override public String getServletContextName() { return "MockServletContext"; }
    @Override public void log(String msg) { System.out.println(msg); }
    @Override public void log(String message, Throwable throwable) { System.out.println(message); throwable.printStackTrace(); }
    @Override public String getRealPath(String path) { return null; }
    @Override public String getServerInfo() { return "MockServlet/6.0"; }

    // ----------------- Servlet / Filter registration -----------------
    @Override public ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) { return null; }
    @Override public <T extends Servlet> T createServlet(Class<T> clazz) { return null; }
    @Override public ServletRegistration getServletRegistration(String servletName) { return null; }
    @Override public Map<String, ? extends ServletRegistration> getServletRegistrations() { return Collections.emptyMap(); }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, String className) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) { return null; }
    @Override public <T extends Filter> T createFilter(Class<T> clazz) { return null; }
    @Override public FilterRegistration getFilterRegistration(String filterName) { return null; }
    @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Collections.emptyMap(); }

    // ----------------- Session -----------------
    @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
    @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) { /* empty */ }
    @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return Set.of(SessionTrackingMode.COOKIE); }
    @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return Set.of(SessionTrackingMode.COOKIE); }
    @Override
	public int getSessionTimeout() { return sessionTimeout; }
    @Override
	public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    // ----------------- Character encoding -----------------
    @Override
	public String getRequestCharacterEncoding() { return requestCharacterEncoding; }
    @Override
	public void setRequestCharacterEncoding(String encoding) { this.requestCharacterEncoding = encoding; }
    @Override
	public String getResponseCharacterEncoding() { return responseCharacterEncoding; }
    @Override
	public void setResponseCharacterEncoding(String encoding) { this.responseCharacterEncoding = encoding; }

    // ----------------- Listeners -----------------
    @Override public void addListener(String className) { /* empty */ }
    @Override public <T extends EventListener> void addListener(T t) { /* empty */ }
    @Override public void addListener(Class<? extends EventListener> listenerClass) { /* empty */ }
    @Override public <T extends EventListener> T createListener(Class<T> clazz) { return null; }
    @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }

    // ----------------- Other -----------------
    @Override public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
    @Override public void declareRoles(String... roleNames) { /* empty */ }
    @Override public String getVirtualServerName() { return "MockServer"; }

    // ----------------- Inner MockRequestDispatcher -----------------
    private static class MockRequestDispatcher implements RequestDispatcher {
        MockRequestDispatcher(@SuppressWarnings("unused") String path) { /* empty */ }
        @Override public void forward(ServletRequest request, ServletResponse response) { /* empty */ }
        @Override public void include(ServletRequest request, ServletResponse response) { /* empty */ }
    }

	@Override
	public Dynamic addJspFile(String arg0, String arg1) {
		return null;
	}
}