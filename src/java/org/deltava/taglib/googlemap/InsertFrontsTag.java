// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to load Weather Underground front data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class InsertFrontsTag extends WeatherJSTag {
	
	private static final String USAGE_ATTR_NAME = "$wxFrontUsage$";

	/**
	 * Constructor. Sets the default function name.
	 */
	public InsertFrontsTag() {
		super("loadFronts");
	}
	
	/**
	 * Executed when tag is rendered. Creates a JavaScript block to dynamically load a wunderground.com front API call.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", USAGE_ATTR_NAME))
			return EVAL_PAGE;
		
		try {
			JspWriter out = pageContext.getOut();
			out.print("<script id=\"wuFrontLoader\" src=\"http://api.wunderground.com/api/***REMOVED***/fronts/view.json?callback=");
			out.print(_jsFunc);
			out.println("\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", USAGE_ATTR_NAME);
		return EVAL_PAGE;
	}
}