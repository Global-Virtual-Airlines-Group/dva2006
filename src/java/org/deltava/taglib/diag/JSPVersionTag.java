// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.diag;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to display the JSP API version.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class JSPVersionTag extends TagSupport {

	/**
	 * Renders the JSP engine version to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			JspEngineInfo info = JspFactory.getDefaultFactory().getEngineInfo();
			out.print(info.getSpecificationVersion());
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}