// Copyright 2005, 2012, 2015, 2018, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import jakarta.servlet.jsp.*;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to insert an ICRA content rating encoded via POWDER.
 * @author Luke
 * @version 12.3
 * @since 1.0
 */

public class InsertPICSTag extends TagSupport {

	/**
	 * Renders the PICS-1.1 content to the JSP output stream. No content will be written if no rating data is found or selected.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "POWDER", "POWDER"))
			return EVAL_PAGE;

		try {
			JspWriter out = pageContext.getOut();
			out.println("<link rel=\"describedby\" href=\"powder.xml\" type=\"application/powder+xml\">");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();	
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "POWDER", "POWDER");
		return EVAL_PAGE;
	}
}