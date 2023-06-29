// Copyright 2005, 2010, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import javax.servlet.jsp.*;

import org.deltava.beans.ViewEntry;

import org.deltava.taglib.html.ElementTag;

/**
 * A JSP Tag to render view table rows with a specified CSS class.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class RowTag extends ElementTag {

	private Object _entry;
	
	/**
	 * Creates the tag.
	 */
	public RowTag() {
		super("tr");
	}
	
	/**
	 * Sets the entry to display in the table row.
	 * @param obj the Object to display.
	 */
	public void setEntry(Object obj) {
		_entry = obj;
	}
	
	/**
	 * Opens the table row by rendering a &lt;TR&gt; tag to the JSP output stream. If a CSS class name has
	 * been specified, then this will be set as the CLASS for the row. If the entry implements the
	 * {@link ViewEntry} interface, then the tableRowClassName property will be used as the class name. If
	 * the class name is NULL, no class property for the row will be set.
	 * @return TagSupport.EVAL_BODY_INCLUDE
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doStartTag() throws JspException {

		// Determine the CSS class name
		if (_classes.isEmpty() && (_entry instanceof ViewEntry ve))
			setClassName(ve.getRowClassName());
		
		super.doStartTag();
		try {
			_out.print(_data.open(true));
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Closes the table row by printing a &lt;/TR&gt; tag to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs 
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			_out.print(_data.close());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}