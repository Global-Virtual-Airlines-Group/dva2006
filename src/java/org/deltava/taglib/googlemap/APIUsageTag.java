// Copyright 2008, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to store the number of times the Google Maps API has been requested since the web application was started.
 * @author Luke
 * @version 9.0
 * @since 2.2
 */

public class APIUsageTag extends TagSupport {

	private String _varName;
	private APIUsage.Type _apiType;
	
	/**
	 * Sets the request attribute name.
	 * @param name the request attribute to store the result in 
	 */
	public void setVar(String name) {
		_varName = name;
	}

	/**
	 * Sets the Google Map API type to request usage for.
	 * @param t the Google Maps API type
	 */
	public void setType(String t) {
		_apiType = APIUsage.Type.valueOf(t.toUpperCase());
	}
	
	/**
	 * Saves the Google Maps usage count in the request.
	 * @return TagSupport.SKIP_BODY always
	 */
	@Override
	public int doStartTag() {
		long useCount = APIUsage.get(_apiType);
		pageContext.setAttribute(_varName, Long.valueOf(useCount), PageContext.REQUEST_SCOPE);
		return SKIP_BODY;
	}
}