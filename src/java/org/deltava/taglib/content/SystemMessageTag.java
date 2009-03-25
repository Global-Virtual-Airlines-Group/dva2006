// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.commands.CommandContext;

/**
 * A JSP tag to display a system message.
 * @author Luke
 * @version 2.4
 * @since 2.4
 * @see CommandContext#SYSMSG_ATTR_NAME
 */

public class SystemMessageTag extends TagSupport {

	/**
	 * Inserts the system message into the JSP.
	 * @return TagSupport.EVAL_PAGE always
	 */
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			Object msg = pageContext.getRequest().getAttribute(CommandContext.SYSMSG_ATTR_NAME);
			if (msg != null)
				out.print(msg);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}