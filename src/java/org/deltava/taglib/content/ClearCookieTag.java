// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to allow pages to clear cookies.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ClearCookieTag extends TagSupport {

    private String _name;
  
    /**
     * Sets the name of the cookie to reset.
     * @param name the cookie name
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Clears the cookie.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    @Override
    public int doEndTag() throws JspException {
        
        // Create the cookie and expire it right away
        Cookie c = new Cookie(_name, "");
        c.setMaxAge(1);
        
        // Save the cookie in the response
        HttpServletResponse rsp = (HttpServletResponse) pageContext.getResponse();
        rsp.addCookie(c);
        
        return EVAL_PAGE;
    }
}