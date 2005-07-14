package org.deltava.taglib.view;

import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.ViewEntry;

/**
 * A JSP Tag to render view table rows with a specified CSS class.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class RowTag extends TagSupport {

	private Object _entry;
	private String _className;
	
	/**
	 * Overrides the CSS class name for the table row. 
	 * @param cName the CSS class name
	 */
	public void setClassName(String cName) {
		_className = cName;
	}
	
	/**
	 * Sets the entry to display in the table row.
	 * @param obj the Object to display.
	 */
	public void setEntry(Object obj) {
		_entry = obj;
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	public void release() {
		super.release();
		_entry = null;
		_className = null;
	}
	
	/**
	 * Opens the table row by rendering a &lt;TR&gt; tag to the JSP output stream. If a CSS class name has
	 * been specified, then this will be set as the CLASS for the row. If the entry implements the
	 * {@link ViewEntry} interface, then the tableRowClassName property will be used as the class name. If
	 * the class name is NULL, no class property for the row will be set.
	 * @return TagSupport.EVAL_BODY_INCLUDE
	 * @throws JspException if an I/O error occurs
	 */
	public int doStartTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			// Determine the CSS class name
			if ((_className == null) && (_entry instanceof ViewEntry))
				_className = ((ViewEntry) _entry).getRowClassName();
			
			// Render the opening tag
			out.print("<tr");
			if (_className != null)
				out.print(" class=\"" + _className + "\"");
			
			out.print('>');
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Closes the table row by printing a &lt;/TR&gt; tag to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs 
	 */
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().write("</tr>");
			release();
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		return EVAL_PAGE;
	}
}