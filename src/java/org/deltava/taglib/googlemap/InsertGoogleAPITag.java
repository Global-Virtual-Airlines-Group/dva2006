// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {

	public static final String USAGE_ATTR_NAME = "$googleMapUsage$";
	
	private int _usageCount;
	private int _apiVersion = 1;

	/**
	 * Sets the Google API version to pull down.
	 * @param ver the API version
	 */
	public void setVersion(int ver) {
		_apiVersion = ver;
	}

	/**
	 * Increments and adds the usage count to the application context.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		_usageCount++;
		pageContext.setAttribute(USAGE_ATTR_NAME, new Integer(_usageCount), PageContext.APPLICATION_SCOPE);
		return super.doStartTag();
	}
	
	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	public int doEndTag() throws JspException {

		// Get the API key
		String apiKey = SystemData.get("security.key.googleMaps");
		if (apiKey == null)
			throw new JspException("Google Maps API key not defined");

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;

		JspWriter out = pageContext.getOut();
		try {
			out
					.print("<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://maps.google.com/maps?file=api&amp;v=");
			out.print(String.valueOf(_apiVersion));
			out.print("&amp;key=");
			out.print(apiKey);
			out.print("\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME);
		return EVAL_PAGE;
	}
}