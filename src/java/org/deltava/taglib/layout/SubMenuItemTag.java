// Copyright 2009, 2010, 2013, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to render CSS sub-menu items.
 * @author Luke
 * @version 8.2
 * @since 2.6
 */

public class SubMenuItemTag extends MenuElementTag {
	
	/**
	 * Writes the sub-menu item's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		
		// Check parent tag
		SubMenuTag parent = (SubMenuTag) TagSupport.findAncestorWithClass(this, SubMenuTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a SUBMENU tag");
		
		try {
			parent.writeSubMenu();
			pageContext.getOut().print(_renderTable ? "<tr class=\"submenu\"><td>" : "<li>");
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
	@Override
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().println(_renderTable ? "</td></tr>" : "</li>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}