// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;

/**
 * A JSP tag to display a navigation menu.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class MenuTag extends MenuElementTag {
	
	private String _attrName;
	
	/**
	 * Sets the page attribute that stores whether a side menu is being rendered.
	 * @param attrName the attribute name
	 */
	public void setAttr(String attrName) {
		_attrName = attrName;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_attrName = null;
	}

	/**
	 * Writes the menu item's opening tag(s) to the JSP output stream. This will
	 * render a TABLE element if the parent region uses tables, otherwise no
	 * output will be generated.
	 * @return EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		if (_renderTable) {
			try {
				pageContext.getOut().println("<table class=\"nav\"><tbody>");
			} catch (Exception e) {
				throw new JspException(e);
			}
		}
		
		if (_attrName != null)
			pageContext.setAttribute(_attrName, Boolean.valueOf(_renderTable), PageContext.PAGE_SCOPE);
		
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Writes the menu item's closing tag(s) to the JSP output stream, if tags
	 * were opened in the {@link MenuTag#doStartTag()} method.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occured
	 */
	public int doEndTag() throws JspException {
		try {
			if (_renderTable)
				pageContext.getOut().println("</tbody></table>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}