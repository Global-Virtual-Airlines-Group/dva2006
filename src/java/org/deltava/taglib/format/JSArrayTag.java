// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to add objects into a JavaScript array.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class JSArrayTag extends TagSupport {

	private String _varName;
	private Collection<Object> _data;

	/**
	 * Sets the JavaScript variable name.
	 * @param varName the variable name
	 */
	public void setVar(String varName) {
		_varName = varName;
	}
	
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
        StringBuilder buf = new StringBuilder("var ");
        buf.append(_varName);
        buf.append(" = [");
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
        }
        
        return EVAL_PAGE;
	}
}