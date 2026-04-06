package org.gvagroup.mockservlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.security.Principal;
import java.util.*;

/**
 * Complete mock HttpServletRequest implementing all methods with practical defaults
 * so the class compiles against the servlet API and is useful in unit tests.
 *
 * Note: behavior is intentionally minimal. Extend for richer semantics as needed.
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private final Map<String, List<String>> headers = new HashMap<>();
    private final Map<String, String[]> parameters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private String method = "GET";
    private String requestURI = "/";
    private String contextPath = "";
    private String servletPath = "";
    private String pathInfo = null;
    private String queryString = null;
    private String protocol = "HTTP/1.1";
    private String scheme = "http";
    private String serverName = "localhost";
    private int serverPort = 80;
    private String remoteAddr = "127.0.0.1";
    private String remoteHost = "localhost";
    private boolean secure = false;
    private String characterEncoding = "UTF-8";
    private byte[] bodyBytes = new byte[0];
    private Cookie[] cookies = null;
    private Locale locale = Locale.getDefault();
    private List<Locale> locales = Collections.singletonList(locale);
    private Principal userPrincipal = null;
    private String authType = null;
    private String remoteUser = null;
    private MockHttpSession session = null;
    private boolean requestedSessionIdValid = false;
    private String requestedSessionId = null;
    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    public MockHttpServletRequest() {}

    // ---------- Convenience setters for tests ----------
    public void setMethod(String method) { this.method = method; }
    public void setRequestURI(String requestURI) { this.requestURI = requestURI; }
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    public void setServletPath(String servletPath) { this.servletPath = servletPath; }
    public void setPathInfo(String pathInfo) { this.pathInfo = pathInfo; }
    public void setQueryString(String queryString) { this.queryString = queryString; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setScheme(String scheme) { this.scheme = scheme; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }
    public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }
    public void setRemoteHost(String remoteHost) { this.remoteHost = remoteHost; }
    public void setSecure(boolean secure) { this.secure = secure; }
    @Override
	public void setCharacterEncoding(String encoding) { this.characterEncoding = encoding; }
    public void setBody(String body) { this.bodyBytes = (body == null ? new byte[0] : body.getBytes()); }
    public void setBody(byte[] bytes) { this.bodyBytes = bytes == null ? new byte[0] : bytes; }
    public void setHeader(String name, String value) {
        List<String> l = new ArrayList<>();
        l.add(value);
        headers.put(name.toLowerCase(Locale.ROOT), l);
    }
    public void addParameter(String name, String... values) { parameters.put(name, values); }
    public void setCookies(Cookie... cookies) { this.cookies = cookies; }
    public void setLocale(Locale locale) { this.locale = locale; this.locales = Collections.singletonList(locale); }
    public void setUserPrincipal(Principal p) { this.userPrincipal = p; this.remoteUser = p == null ? null : p.getName(); }
    public void setAuthType(String authType) { this.authType = authType; }
    public void setSession(MockHttpSession session) { this.session = session; }
    public void setRequestedSessionId(String id) { this.requestedSessionId = id; this.requestedSessionIdValid = id != null; }

    // ---------- ServletRequest methods ----------
    @Override
    public Object getAttribute(String name) { return attributes.get(name); }

    @Override
    public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }

    @Override
    public String getCharacterEncoding() { return characterEncoding; }

    @Override
    public int getContentLength() { return bodyBytes.length; }

    @Override
    public long getContentLengthLong() { return bodyBytes.length; }

    @Override
    public String getContentType() { return getHeader("content-type"); }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener readListener) { /* no-op */ }
            @Override public int read() { return bais.read(); }
        };
    }

    @Override
    public String getParameter(String name) {
        String[] vals = parameters.get(name);
        return (vals == null || vals.length == 0) ? null : vals[0];
    }

    @Override
    public Enumeration<String> getParameterNames() { return Collections.enumeration(parameters.keySet()); }

    @Override
    public String[] getParameterValues(String name) { return parameters.get(name); }

    @Override
    public Map<String, String[]> getParameterMap() { return parameters; }

    @Override
    public String getProtocol() { return protocol; }

    @Override
    public String getScheme() { return scheme; }

    @Override
    public String getServerName() { return serverName; }

    @Override
    public int getServerPort() { return serverPort; }

    @Override
    public BufferedReader getReader() throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(bodyBytes), characterEncoding);
        return new BufferedReader(isr);
    }

    @Override
    public String getRemoteAddr() { return remoteAddr; }

    @Override
    public String getRemoteHost() { return remoteHost; }

    @Override
    public void setAttribute(String name, Object o) { attributes.put(name, o); }

    @Override
    public void removeAttribute(String name) { attributes.remove(name); }

    @Override
    public Locale getLocale() { return locale; }

    @Override
    public Enumeration<Locale> getLocales() { return Collections.enumeration(locales); }

    @Override
    public boolean isSecure() { return secure; }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) { return new MockRequestDispatcher(path); }

    @Override
    public int getRemotePort() { return 0; }

    @Override
    public String getLocalName() { return serverName; }

    @Override
    public String getLocalAddr() { return serverName; }

    @Override
    public int getLocalPort() { return serverPort; }

    @Override
    public ServletContext getServletContext() { return null; }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("Async not supported in this mock");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new IllegalStateException("Async not supported in this mock");
    }

    @Override
    public boolean isAsyncStarted() { return false; }

    @Override
    public boolean isAsyncSupported() { return false; }

    @Override
    public AsyncContext getAsyncContext() { return null; }

    @Override
    public DispatcherType getDispatcherType() { return dispatcherType; }

    // ---------- HttpServletRequest methods ----------
    @Override
    public String getAuthType() { return authType; }

    @Override
    public Cookie[] getCookies() { return cookies; }

    @SuppressWarnings("deprecation")
	@Override
    public long getDateHeader(String name) {
        String v = getHeader(name);
        if (v == null) return -1;
        try {
            return Date.parse(v);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String getHeader(String name) {
        List<String> vals = headers.get(name == null ? null : name.toLowerCase(Locale.ROOT));
        return (vals == null || vals.isEmpty()) ? null : vals.get(0);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> vals = headers.get(name == null ? null : name.toLowerCase(Locale.ROOT));
        return vals == null ? Collections.emptyEnumeration() : Collections.enumeration(vals);
    }

    @Override
    public Enumeration<String> getHeaderNames() { return Collections.enumeration(headers.keySet()); }

    @Override
    public int getIntHeader(String name) {
        String v = getHeader(name);
        if (v == null) return -1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getMethod() { return method; }

    @Override
    public String getPathInfo() { return pathInfo; }

    @Override
    public String getPathTranslated() { return null; }

    @Override
    public String getContextPath() { return contextPath; }

    @Override
    public String getQueryString() { return queryString; }

    @Override
    public String getRemoteUser() { return remoteUser; }

    @Override
    public boolean isUserInRole(String role) { return false; }

    @Override
    public Principal getUserPrincipal() { return userPrincipal; }

    @Override
    public String getRequestedSessionId() { return requestedSessionId; }

    @Override
    public String getRequestURI() { return requestURI; }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer sb = new StringBuffer();
        sb.append(scheme).append("://").append(serverName);
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            sb.append(":").append(serverPort);
        }
        sb.append(requestURI);
        return sb;
    }

    @Override
    public String getServletPath() { return servletPath; }

    @Override
    public HttpSession getSession(boolean create) {
        if (session == null && create) {
            session = new MockHttpSession(getServletContext());
            requestedSessionId = session.getId();
            requestedSessionIdValid = true;
        }
        return session;
    }

    @Override
    public HttpSession getSession() { return getSession(true); }

    @Override
    public boolean isRequestedSessionIdValid() { return requestedSessionIdValid; }

    @Override
    public boolean isRequestedSessionIdFromCookie() { return true; }

    @Override
    public boolean isRequestedSessionIdFromURL() { return false; }

    // ---------- Servlet 3.0+ convenience methods (minimal implementations) ----------
    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new ServletException("authenticate not supported in mock");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new ServletException("login not supported in mock");
    }

    @Override
    public void logout() throws ServletException {
        throw new ServletException("logout not supported in mock");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return Collections.emptyList();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new ServletException("upgrade not supported in mock");
    }

    // ---------- HTTP/2 and newer helper defaults (keep minimal) ----------
    public long getContentLengthLong(@SuppressWarnings("unused") String s) { return getContentLengthLong(); } // not part of API, just safe default

    // ---------- Utility inner mock for RequestDispatcher ----------
    private static class MockRequestDispatcher implements RequestDispatcher {
        MockRequestDispatcher(@SuppressWarnings("unused") String path) { /* empty */  }
        @Override public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            // no-op for tests
        }
        @Override public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            // no-op for tests
        }
    }

    // ---------- equals/hashCode for easier testing ----------
    @Override
    public String toString() {
        return "MockHttpServletRequest[" + method + " " + requestURI + "]";
    }

	@Override
	public String getProtocolRequestId() {
		return null;
	}

	@Override
	public String getRequestId() {
		return null;
	}

	@Override
	public ServletConnection getServletConnection() {
		return null;
	}

	@Override
	public String changeSessionId() {
		return null;
	}
}
