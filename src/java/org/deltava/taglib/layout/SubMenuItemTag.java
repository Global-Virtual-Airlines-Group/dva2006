// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to render CSS sub-menu items.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class SubMenuItemTag extends MenuElementTag {
	
	private String _className;
	
	/**
	 * Sets the CSS style to use for the rendered element.
	 * @param cName the CSS selector(s)
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
	 * Writes the sub-menu item's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		
		// Check parent tag
		SubMenuTag parent = (SubMenuTag) TagSupport.findAncestorWithClass(this, SubMenuTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a SUBMENU tag");
		
		// Check what we're writing
		try {
			JspWriter out = pageContext.getOut();
			parent.writeSubMenu();
			if (_renderTable) {
				out.print("<tr class=\"submenu");
				if (!StringUtils.isEmpty(_className)) {
					out.print(" ");
					out.print(_className);
				}
				
				out.print("\"><td>");
			} else {
				out.print("<li");
				if (!StringUtils.isEmpty(_className)) {
					out.print(" class=\"");
					out.print(_className);
					out.print('"');
				}
				
				out.print('>');
			}
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the sub-menu item's closing tag to the JSP output stream.
	 * 	@return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print(_renderTable ? "</td></tr>" : "</li>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}