// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.*;

/**
 * A JSP tag to support writing formatted text. 
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class TextFormatTag extends TagSupport {

    private String _value;
    private String _className;
    private String _default;

    /**
     * Sets the value to format.
     * @param value the value to format
     */
    public void setValue(String value) {
        _value = value;
    }
    
    /**
     * Sets the default value if the provided value is null.
     * @param defValue the default value
     */
    public void setDefault(String defValue) {
    	_default = defValue;
    }
    
    /**
     * Updates the CSS class for this formatted number. This will automatically enclose the output in a
     * &lt;SPAN&gt; tag.
     * @param cName the class Name(s)
     */
    public void setClassName(String cName) {
        _className = cName;
    }
    
    /**
     * Releases the tag's state variables. 
     */
    public void release() {
        super.release();
        _className = null;
    }
    
    /**
     * Formats the text and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     * @see StringUtils#stripInlineHTML(String)
     */
    public int doEndTag() throws JspException {
    	
        JspWriter out = pageContext.getOut();
        try {
            if (_className != null) {
                out.print("<span class=\"");
                out.print(_className);
                out.print("\">");
            }
            
            out.print((_value == null) ? _default : StringUtils.stripInlineHTML(_value));
            if (_className != null)
                out.print("</span>");
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
    }
}