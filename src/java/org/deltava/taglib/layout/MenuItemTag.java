// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;

/**
 * A JSP tag to render a top level menu item in a JSP tag.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class MenuItemTag extends MenuElementTag {
	
	private int _width;
	
	/**
	 * Sets the width of the menu title.
	 * @param width the width in pixels
	 */
	public void setWidth(int width) {
		_width = Math.max(0, width);
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_width = 0;
	}
	
	/**
	 * Writes the menu item's opening tag(s) to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable)
				out.print("<tr class=\"menuitem\"><td>");
			else {
				out.print("<ul class=\"menuitem\"");
				if (_width > 0)
					out.print(" style=\"width:" + String.valueOf(_width) + "px;\"");
				out.print("><li>");
			}
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the menu item's closing tag(s) to the JSP output stream.
	 * 	@return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			if (!_renderTable)
				pageContext.getOut().print("</li></ul>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}