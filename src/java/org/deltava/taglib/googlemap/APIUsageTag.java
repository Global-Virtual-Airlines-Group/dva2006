// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import static org.deltava.taglib.googlemap.InsertGoogleAPITag.USAGE_ATTR_NAME;

/**
 * A JSP tag to store the number of times a Google Map has been displayed since the
 * web application was started.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class APIUsageTag extends TagSupport {

	private String _varName;
	
	/**
	 * Sets the request attribute name.
	 * @param name the request attribute to store the result in 
	 */
	public void setVar(String name) {
		_varName = name;
	}
	
	/**
	 * Saves the Google Maps usage count in the request.
	 * @return TagSupport.SKIP_BODY always
	 */
	public int doStartTag() {
		Integer useCount = (Integer) pageContext.getServletContext().getAttribute(USAGE_ATTR_NAME);
		pageContext.setAttribute(_varName, useCount, PageContext.REQUEST_SCOPE);
		return SKIP_BODY;
	}
}