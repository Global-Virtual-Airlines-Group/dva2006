// Copyright 2009, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to render a top level menu item in a JSP tag.
 * @author Luke
 * @version 8.2
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
	@Override
	public void release() {
		super.release();
		_width = 0;
	}
	
	/**
	 * Writes the menu item's opening tag(s) to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable)
				out.print("<tr class=\"menuitem\"><td>");
			else {
				MenuTag parent = (MenuTag) TagSupport.findAncestorWithClass(this, MenuTag.class);
				if (parent == null)
					throw new JspException("Not contained within MenuTag");
				
				out.print("<ul class=\"menuitem\"");
				if (_width > 0) {
					out.print(" style=\"width:");
					out.print(_width);
					out.print("px");
					
					// Render maxWidth if present
					String maxWidth = parent.getMaxMenuWidth();
					if (!StringUtils.isEmpty(maxWidth)) {
						out.print(";max-width:");
						out.print(maxWidth);
						if (!maxWidth.endsWith("%"))
							out.print("px");
					}
					
					out.print(";\"");
				}
				
				out.print("><li><span>");
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
	@Override
	public int doEndTag() throws JspException {
		try {
			if (!_renderTable)
				pageContext.getOut().println("</span></li></ul>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}