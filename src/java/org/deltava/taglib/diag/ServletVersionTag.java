// Copyright 2008, 2016, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.diag;

import jakarta.servlet.jsp.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to display the Servlet API version.
 * @author Luke
 * @version 12.3
 * @since 2.2
 */

public class ServletVersionTag extends TagSupport {

	/**
	 * Renders the Servlet API version to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
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