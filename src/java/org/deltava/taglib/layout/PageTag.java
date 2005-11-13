// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.BrowserDetectingTag;

/**
 * A JSP tag to render page layouts in a browser-specific way. On Mozilla, absolutely positioned DIV elements will be
 * used, while tables will be used for Internet Explorer.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PageTag extends BrowserDetectingTag {

	/**
	 * Writes the layout element's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		// Do nothing for Mozilla
		if (isFirefox())
			return EVAL_BODY_INCLUDE;

		// Render a table for IE
		try {
			pageContext.getOut().print("<table class=\"ieLayout\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Include the body
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the layout element's closing tag to the JSP output stream.
	 * @return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		if (isIE()) {
			try {
				pageContext.getOut().print("</tr></table>");
			} catch (Exception e) {
				throw new JspException(e);
			}
		}

		// Release and return
		release();
		return EVAL_PAGE;
	}
}