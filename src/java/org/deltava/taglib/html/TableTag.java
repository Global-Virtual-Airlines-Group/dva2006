// Copyright 2005, 2010, 2012 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to render HTML tables.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class TableTag extends ElementTag {

	/**
	 * Creates a new Table tag.
	 */
	public TableTag() {
		super("table");
	}

	/**
	 * Sets the WIDTH value for this table.
	 * @param width the width attribute value
	 */
	public void setWidth(String width) {
		_data.setAttribute("width", width);
	}

	/**
	 * Opens this TABLE element by writing a &gt;TABLE&lt; tag.
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		try {
			_out.print(_data.open(true));
		} catch (Exception e) {
			throw new JspException(e);
		}

		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Closes this TABLE element by writing a &gt;/TABLE&lt; tag.
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