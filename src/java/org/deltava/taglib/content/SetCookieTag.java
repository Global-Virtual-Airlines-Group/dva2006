// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to allow pages to set cookies.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetCookieTag extends TagSupport {

    private String _name;
    private String _value;
    private String _domain;
    private String _path;
    
    private int _maxAge = -1;

    /**
     * Updates the cookie's name.
     * @param name the cookie name
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Updates the cookie's value. Non-text data should be escaped using Base64.
     * @param value the cookie value
     */
    public void setValue(String value) {
        _value = value;
    }
    
    /**
     * Updates the cookie's domain for visibility.
     * @param domain the cookie domain
     */
    public void setDomain(String domain) {
        _domain = domain;
    }
    
    /**
     * Updates the cookie's path for visibility.
     * @param path the cookie path
     */
    public void setPath(String path) {
        _path = path;
    }
    
    /**
     * Updates the maximum age of the cookie. Use -1 for session cookies.
     * @param maxAge the age of the cookie in seconds
     */
    public void setAge(int maxAge) {
        _maxAge = (maxAge >= -1) ? maxAge : -1;
    }
    
    /**
     * Releases the tag's state.
     */
    @Override
    public void release() {
        super.release();
        _domain = null;
        _path = null;
        _maxAge = -1;
    }
    
    /**
     * Sets the cookie in the HTTP resposne.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    @Override
    public int doEndTag() throws JspException {
        
        // Create the cookie
        Cookie c = new Cookie(_name, _value);
        c.setVersion(1);
        c.setDomain(_domain);
        c.setPath(_path);
        c.setMaxAge(_maxAge);
        
        // Add the cookie to the response
        HttpServletResponse rsp = (HttpServletResponse) pageContext.getResponse();
        rsp.addCookie(c);
        
        release();
        return EVAL_PAGE;
    }
}