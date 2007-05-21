// Copyright 2004, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.text.*;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;

/**
 * A JSP tag to support the rendering of formatted numeric values.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class NumberFormatTag extends TagSupport {

    protected DecimalFormat _nF;
    protected Number _value;
    protected String _className;
    
    /**
     * Initializes the tag.
     */
    public NumberFormatTag() {
        super();
    }
    
    /**
     * Updates the CSS class for this formatted number. This will automatically enclose the output in a
     * &lt;SPAN&gt; tag.
     * @param cName the class Name(s)
     */
    public final void setClassName(String cName) {
        _className = cName;
    }
    
    /**
     * Updates the date format pattern.
     * @param pattern the format pattern string
     * @see DecimalFormat#applyPattern(String)
     */
    public final void setFmt(String pattern) {
        _nF.applyPattern(pattern);
    }
    
    /**
     * Sets the value to format.
     * @param value the value to format
     */
    public final void setValue(Number value) {
        _value = value;
    }
    
    /**
     * Releases the tag's state and resets the format pattern string. 
     * @param pattern the new format pattern
     */
    protected void release(String pattern) {
        super.release();
        _className = null;
        _nF.applyPattern(pattern);
    }
    
    /**
     * Updates this tag's page context and loads the user object from the request.
     * @param ctxt the new JSP page context
     */
    public final void setPageContext(PageContext ctxt) {
        super.setPageContext(ctxt);
        HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
        Principal user = req.getUserPrincipal();
        if (user instanceof Person)
            _nF.applyPattern(((Person) user).getNumberFormat());
    }
    
    /**
     * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            if (_className != null) {
                out.print("<span class=\"");
                out.print(_className);
                out.print("\">");
                out.print(_nF.format(_value.doubleValue()));
                out.print("</span>");
            } else
            	out.print(_nF.format(_value.doubleValue()));
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
    }
}