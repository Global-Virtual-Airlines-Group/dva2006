// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;
import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.ComboAlias;

/**
 * A JSP tag to format List values.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ListFormatTag extends TagSupport {

    private Collection _values;
    private String _delim;
    
    /**
     * Sets the delimiter for the list entries.
     * @param delim the delimiter
     */
    public void setDelim(String delim) {
        _delim = delim;
    }
    
    /**
     * Sets the values to display.
     * @param c a Collection of objects
     */
    public void setValue(Collection c) {
        _values = c;
    }
    
    /**
     * Renders the object as a delimited string to the JSP output stream. If an object in the Collection is a
     * {@link ComboAlias} object, then the comboName property will be rendered.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     * @see ComboAlias#getComboName()
     */
    public int doEndTag() throws JspException {
        
        // Generate the output string
        StringBuffer buf = new StringBuffer();
        for (Iterator i = _values.iterator(); i.hasNext(); ) {
            Object obj = i.next();
            if (obj instanceof ComboAlias) {
                buf.append(((ComboAlias) obj).getComboName());
            } else {
                buf.append(String.valueOf(obj));
            }
            
            if (i.hasNext())
                buf.append(_delim);
        }
        
        try {
            pageContext.getOut().write(buf.toString());
        } catch (IOException ie) {
            JspException je = new JspException(ie.getMessage());
            je.initCause(ie);
            throw je;
        }
        
        return EVAL_PAGE;
    }
}