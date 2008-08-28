// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.diag;

import javax.servlet.jsp.*;
import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to display the Servlet API version.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class ServletVersionTag extends TagSupport {

	/**
	 * Renders the Servlet API version to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			ServletContext ctx = pageContext.getServletContext();
			out.print(ctx.getMajorVersion());
			out.print('.');
			out.print(ctx.getMinorVersion());
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}