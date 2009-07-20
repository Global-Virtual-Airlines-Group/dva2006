// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {

	public static final String USAGE_ATTR_NAME = "$googleMapUsage$";
	private static final AtomicLong USAGE_COUNT = new AtomicLong();

	private int _majorVersion = 2;
	private String _minorVersion;
	private boolean _doCurrent = false;
	private boolean _doStable = false;

	/**
	 * Sets the Google API version to pull down.
	 * @param ver the API major version
	 */
	public void setVersion(int ver) {
		_majorVersion = ver;
	}

	/**
	 * Sets the Google API revision to pull down.
	 * @param ver the API minor version.
	 */
	public void setMinor(String ver) {
		_minorVersion = ver;
	}

	/**
	 * Controls whether the pre-release API version should be used.
	 * @param doCurrent TRUE if the pre-release API should be used, otherwise FALSE
	 */
	public void setCurrent(boolean doCurrent) {
		_doCurrent = doCurrent;
	}
	
	/**
	 * Controls whether the stable API version should be used.
	 * @param doStable TRUE if the stable API should be used, otherwise FALSE
	 */
	public void setStable(boolean doStable) {
		_doStable = doStable;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_doCurrent = false;
		_minorVersion = null;
	}

	/**
	 * Increments and adds the usage count to the application context.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		long value = USAGE_COUNT.incrementAndGet();
		if (value == 1)
			pageContext.setAttribute(USAGE_ATTR_NAME, USAGE_COUNT, PageContext.APPLICATION_SCOPE);
		
		return super.doStartTag();
	}

	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	public int doEndTag() throws JspException {

		// Get the API keymap
		Map<?, ?> apiKeys = (Map<?, ?>) SystemData.getObject("security.key.googleMaps");
		if ((apiKeys == null) || (apiKeys.isEmpty()))
			throw new JspException("Google Maps API keys not defined");

		// Get the API key for this hostname
		String hostName = pageContext.getRequest().getServerName().toLowerCase();
		String apiKey = (String) apiKeys.get(hostName);
		if (apiKey == null)
			apiKey = (String) apiKeys.values().iterator().next();

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;

		JspWriter out = pageContext.getOut();
		try {
			out.print("<script language=\"JavaScript\" src=\"http://maps.google.com/maps?file=api&amp;v=");
			out.print(String.valueOf(_majorVersion));
			if (_doStable)
				out.print(".s");
			else if (_doCurrent)
				out.print(".x");
			else if (_minorVersion != null)
				out.print(_minorVersion);

			out.print("&amp;key=");
			out.print(apiKey);
			out.print("\" type=\"text/javascript\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME);
		release();
		return EVAL_PAGE;
	}
}