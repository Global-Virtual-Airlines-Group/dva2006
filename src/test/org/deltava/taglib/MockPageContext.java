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

    @Override
	public void initialize(Servlet srv, ServletRequest req, ServletResponse rsp, String errorURL, boolean needsSession, int bufferSize, boolean autoFlush) {
        _req = (HttpServletRequest) req;
        _rsp = (HttpServletResponse) rsp;
    }

    @Override
	public void release() {
        throw new UnsupportedOperationException();
    }
    
    @Override
	public javax.el.ELContext getELContext() {
    	throw new UnsupportedOperationException();
    }
    
    @Override
	@Deprecated
    public VariableResolver getVariableResolver() {
        throw new UnsupportedOperationException();
    }
    
    @Override
	@Deprecated
    public ExpressionEvaluator getExpressionEvaluator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
	public void setAttribute(String key, Object obj) {
        _attrs.put(key, obj);
    }

    @Override
	public void setAttribute(String key, Object obj, int scope) {
        setAttribute(key, obj);
    }

    @Override
	public Object getAttribute(String key) {
        return _attrs.get(key);
    }

    @Override
	public Object getAttribute(String key, int scope) {
        return getAttribute(key);
    }

    @Override
	public Object findAttribute(String key) {
        return getAttribute(key);
    }

    @Override
	public void removeAttribute(String key) {
        _attrs.remove(key);
    }

    @Override
	public void removeAttribute(String key, int scope) {
        removeAttribute(key);
    }

    @Override
	public int getAttributesScope(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Enumeration<String> getAttributeNamesInScope(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
	public JspWriter getOut() {
        return _out;
    }

    @Override
	public HttpSession getSession() {
        return _req.getSession();
    }

    @Override
	public Object getPage() {
        throw new UnsupportedOperationException();
    }

    @Override
	public ServletRequest getRequest() {
        return _req;
    }

    @Override
	public ServletResponse getResponse() {
        return _rsp;
    }

    @Override
	public Exception getException() {
        return null;
    }

    @Override
	public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
	public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
	public void forward(String arg0) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void include(String arg0) {
        throw new UnsupportedOperationException();
    }
    
    @Override
	public void include(String arg0, boolean arg1) {
        if (arg1)
            include(arg0);
    }

    @Override
	public void handlePageException(Exception arg0) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void handlePageException(Throwable arg0) throws ServletException,
            IOException {
        throw new UnsupportedOperationException();
    }
}