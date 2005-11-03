// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.Map;

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
	private static int USAGE_COUNT = 0;

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
		USAGE_COUNT++;
		pageContext.setAttribute(USAGE_ATTR_NAME, new Integer(USAGE_COUNT), PageContext.APPLICATION_SCOPE);
		return super.doStartTag();
	}

	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	public int doEndTag() throws JspException {

		// Get the API keymap
		Map apiKeys = (Map) SystemData.getObject("security.key.googleMaps");
		if (apiKeys == null)
			throw new JspException("Google Maps API keys not defined");

		// Get the API key for this hostname
		String hostName = pageContext.getRequest().getServerName().toLowerCase();
		String apiKey = (String) apiKeys.get(hostName);
		if (apiKey == null)
			throw new JspException("Cannot find Google Maps API key for " + hostName);

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;

		JspWriter out = pageContext.getOut();
		try {
			out.print("<script language=\"JavaScript\" src=\"http://maps.google.com/maps?file=api&amp;v=");
			out.print(String.valueOf(_apiVersion));
			out.print("&amp;key=");
			out.print(apiKey);
			out.print("\" type=\"text/javascript\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME);
		return EVAL_PAGE;
	}
}