// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.JSTag;

/**
 * A JSP tag to add objects into a JavaScript array.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class JSArrayTag extends JSTag {

	private Collection<Object> _data;

	/**
	 * Sets the items to put into the JavaScript array.
	 * @param items a Collection of objects.
	 */
	public void setItems(Collection<Object> items) {
		_data = items;
	}
	
	/**
	 * Renders the JavaScript array to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		
        // Generate the output string
        StringBuilder buf = new StringBuilder();
        if (_jsVarName != null) {
        	buf.append("var ");
        	buf.append(_jsVarName);
        	buf.append(" = ");
        }
        
        buf.append('[');
        for (Iterator<Object> i = _data.iterator(); i.hasNext(); ) {
        	Object obj = i.next();
        	if (obj instanceof Number)
        		buf.append(obj.toString());
        	else if (obj == null)
        		buf.append("null");
        	else {
        		buf.append('\'');
        		buf.append(String.valueOf(obj));
        		buf.append('\'');
        	}
        	
        	if (i.hasNext())
        		buf.append(',');
        }
        
        // Write the object
        buf.append("];");
        try {
        	pageContext.getOut().write(buf.toString());
        } catch (Exception e) {
        	throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
	}
}