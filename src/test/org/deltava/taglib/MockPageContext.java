package org.deltava.taglib;

import java.io.IOException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.*;

public class MockPageContext extends PageContext {

    private Map<String, Object> _attrs;
    private HttpServletRequest _req;
    private HttpServletResponse _rsp;
    private JspWriter _out;

    public MockPageContext(JspWriter jspOut) {
        super();
        _attrs = new HashMap<String, Object>();
        _out = jspOut;
    }

    public void initialize(Servlet srv, ServletRequest req,
            ServletResponse rsp, String errorURL, boolean needsSession,
            int bufferSize, boolean autoFlush) {
        _req = (HttpServletRequest) req;
        _rsp = (HttpServletResponse) rsp;
    }

    public void release() {
        throw new UnsupportedOperationException();
    }
    
    public javax.el.ELContext getELContext() {
    	throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public VariableResolver getVariableResolver() {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public ExpressionEvaluator getExpressionEvaluator() {
        throw new UnsupportedOperationException();
    }
    
    public void setAttribute(String key, Object obj) {
        _attrs.put(key, obj);
    }

    public void setAttribute(String key, Object obj, int scope) {
        setAttribute(key, obj);
    }

    public Object getAttribute(String key) {
        return _attrs.get(key);
    }

    public Object getAttribute(String key, int scope) {
        return getAttribute(key);
    }

    public Object findAttribute(String key) {
        return getAttribute(key);
    }

    public void removeAttribute(String key) {
        _attrs.remove(key);
    }

    public void removeAttribute(String key, int scope) {
        removeAttribute(key);
    }

    public int getAttributesScope(String arg0) {
        throw new UnsupportedOperationException();
    }

    public Enumeration<String> getAttributeNamesInScope(int arg0) {
        throw new UnsupportedOperationException();
    }

    public JspWriter getOut() {
        return _out;
    }

    public HttpSession getSession() {
        return _req.getSession();
    }

    public Object getPage() {
        throw new UnsupportedOperationException();
    }

    public ServletRequest getRequest() {
        return _req;
    }

    public ServletResponse getResponse() {
        return _rsp;
    }

    public Exception getException() {
        return null;
    }

    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException();
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public void forward(String arg0) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    public void include(String arg0) {
        throw new UnsupportedOperationException();
    }
    
    public void include(String arg0, boolean arg1) {
        if (arg1)
            include(arg0);
    }

    public void handlePageException(Exception arg0) throws ServletException,
            IOException {
        throw new UnsupportedOperationException();
    }

    public void handlePageException(Throwable arg0) throws ServletException,
            IOException {
        throw new UnsupportedOperationException();
    }
}