// Copyright 2005, 2016 Global Virtual Ailrines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to allow pages to read cookie data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetCookieTag extends TagSupport {

    private String _name;
    private String _default;
    private String _var;
    
    /**
     * Sets the name of the cookie to read.
     * @param name the cookie name
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Sets the default value if the cookie is not found.
     * @param dValue the default value
     */
    public void setDefault(String dValue) {
        _default = dValue;
    }
    
    /**
     * Sets the request attribute name to set with the cookie/default value.
     * @param varName the attribute name
     */
    public void setVar(String varName) {
        _var = varName;
    }
    
    /**
     * Releases this tag's state and default value.
     */
    @Override
    public void release() {
        super.release();
        _default = null;
    }
    
    /**
     * Helper method to fetch a cookie value.
     */
    private String getCookie(String cName) {
        
        // Get the cookies from the request
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
            return null;
        
        // Search for our cookie
        for (int x = 0; x < cookies.length; x++) {
            if (cookies[x].getName().equals(cName))
                return cookies[x].getValue();
        }
        
        return null;
    }
    
    /**
     * Reads the cookie value and stores it in the request.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    @Override
    public int doEndTag() throws JspException {
        
        // Get the cookie value that we want; if we don't get it, search for the default value
        String cValue = getCookie(_name);
        if (cValue == null)
            cValue = _default;
        
        // If we got a value, stuff it in the request
        if (cValue != null)
            pageContext.getRequest().setAttribute(_var, cValue);
        
        release();
        return EVAL_PAGE;
    }
}