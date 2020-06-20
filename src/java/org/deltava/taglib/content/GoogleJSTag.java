// Copyright 2012, 2013, 2017, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to embed the Google asynchronous JavaScript API. 
 * @author Luke
 * @version 9.0
 * @since 5.0
 */

public class GoogleJSTag extends TagSupport {
	
	private String _module;
	
	/**
	 * Sets the module name to load.
	 * @param moduleName
	 */
	public void setModule(String moduleName) {
		_module = moduleName;
	}

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
			out.print("<script src=\"https://www.gstatic.com/");
			out.print(_module);
			out.println("/loader.js\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", "GoogleJSAPI");
		return EVAL_PAGE;
	}
}