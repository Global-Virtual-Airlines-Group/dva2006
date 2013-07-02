// Copyright 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to embed the Google asynchronous JavaScript API. 
 * @author Luke
 * @version 5.1
 * @since 5.0
 */

public class GoogleJSTag extends TagSupport {

	/**
	 * Renders the tag.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "JS", "GoogleJSAPI"))
			return EVAL_PAGE;
		
		try {
			JspWriter out = pageContext.getOut();
			out.print("<script src=\"http");
			if (pageContext.getRequest().isSecure())
				out.print('s');
			
			out.println("://www.google.com/jsapi\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", "GoogleJSAPI");
		return EVAL_PAGE;
	}
}