// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A class to support the rendering of numbers in hexadecimal.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HexFormatTag extends TagSupport {
    
    private String _className;
    private long _value;

    /**
     * Updates the CSS class for this formatted number. This will automatically enclose the output in a
     * &lt;SPAN&gt; tag.
     * @param cName the class Name(s)
     */
    public final void setClassName(String cName) {
        _className = cName;
    }
    
    /**
     * Sets the value to format.
     * @param value the value to format
     */
    public final void setValue(long value) {
        _value = value;
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
            }
            
            try {
                out.print(Long.toHexString(_value));
            } catch (NumberFormatException nfe) {
                out.print(_value);
            }
            
            if (_className != null)
                out.print("</span>");
        } catch (IOException ie) {
            JspException je = new JspException(ie.getMessage());
            je.initCause(ie);
            throw je;
        }

        release();
        return EVAL_PAGE;
    }
}