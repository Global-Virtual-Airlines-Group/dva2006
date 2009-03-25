// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.commands.CommandContext;

/**
 * A JSP Tag to display content if a system message is in the Request.
 * @author Luke
 * @version 2.4
 * @since 2.4
 * @see CommandContext#SYSMSG_ATTR_NAME
 */

public class SystemMessageFilterTag extends TagSupport {

	/**
	 * Filters the content of this tag based on whether a system message is present
	 * in the request.
	 * @return SKIP_BODY if message not found, otherwise EVAL_BODY_INCLUDE
	 */
	public int doStartTag() {
		Object msg = pageContext.getRequest().getAttribute(CommandContext.SYSMSG_ATTR_NAME);
		return (msg != null) ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}