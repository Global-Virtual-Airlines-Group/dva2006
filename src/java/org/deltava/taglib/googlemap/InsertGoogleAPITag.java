// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {

	static final String USAGE_ATTR_NAME = "$googleMapUsage$";
	static final String API_VER_ATTR_NAME = "$googleMapAPIVersion$";
	
	private static final int MIN_API_VERSION = 2;
	
	private static final String V2_API_URL = "http://maps.google.com/maps?file=api&amp;v=";
	private static final String V3_API_URL = "http://maps.googleapis.com/maps/api/js?sensor=false&amp;v=";
	
	private static final AtomicLong USAGE_COUNT = new AtomicLong();

	private int _majorVersion = MIN_API_VERSION;
	private String _minorVersion;
	private final Collection<String> _libraries = new LinkedHashSet<String>();

	/**
	 * Sets the Google API version to pull down.
	 * @param ver the API major version
	 */
	public void setVersion(int ver) {
		_majorVersion = Math.max(MIN_API_VERSION, ver);
	}

	/**
	 * Sets the Google API revision to pull down.
	 * @param ver the API minor version.
	 */
	public void setMinor(String ver) {
		_minorVersion = ver;
	}
	
	/**
	 * Sets the Google Maps v3 libraries to load.
	 * @param libList a comma-separated list of libraries.
	 */
	public void setLibraries(String libList) {
		_libraries.addAll(StringUtils.split(libList, ","));
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_libraries.clear();
		_majorVersion = MIN_API_VERSION;
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
		
		// Translate stable/release v3 to minor version
		if ((_majorVersion == 3) && (_minorVersion == null))
			_minorVersion = "8";
		else if ((_majorVersion == 2) && (_minorVersion == null))
			_minorVersion = "s";
		
		return super.doStartTag();
	}

	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	public int doEndTag() throws JspException {

		// Get the API key
		String apiKey = null;
		if (_majorVersion < 3) {
			Map<?, ?> apiKeys = (Map<?, ?>) SystemData.getObject("security.key.googleMaps");
			if ((_majorVersion < 3) && (apiKeys == null) || (apiKeys.isEmpty()))
				throw new JspException("Google Maps API keys not defined");

			// Get the API key for this hostname
			String hostName = pageContext.getRequest().getServerName().toLowerCase();
			apiKey = (String) apiKeys.get(hostName);
			if (apiKey == null)
				apiKey = (String) apiKeys.values().iterator().next();
		}

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;
		
		// Insert the API version
		pageContext.setAttribute(API_VER_ATTR_NAME, Integer.valueOf(_majorVersion), PageContext.REQUEST_SCOPE);
		
		JspWriter out = pageContext.getOut();
		try {
			out.print("<script type=\"text/javascript\" src=\"");
			out.print((_majorVersion == 3) ? V3_API_URL : V2_API_URL);
			out.print(String.valueOf(_majorVersion));
			if (_minorVersion != null) {
				out.print('.');
				out.print(_minorVersion);
			}

			if (_majorVersion < 3) {
				out.print("&amp;key=");
				out.print(apiKey);
			} else if (!_libraries.isEmpty()) {
				out.print("&amp;libraries=");
				for (Iterator<String> i = _libraries.iterator(); i.hasNext(); ) {
					out.print(i.next());
					if (i.hasNext())
						out.print(',');
				}
			}
			
			out.println("\"></script>");
			
			// Init common code
			out.println("<script type=\"text/javascript\">");
			out.print("var golgotha = {maps: { IMG_PATH:\'");
			out.print(SystemData.get("path.img"));		
			out.print("\', API:");
			out.print(String.valueOf(_majorVersion));
			out.print(", tileHost:\'");
			out.print(SystemData.get("weather.tileHost"));
			out.print("\', multiHost:");
			out.print(SystemData.getBoolean("weather.multiHost"));
			out.print(", seriesData: {} ");
			out.println("}};</script>");
			
			// Add JS support file
			String jsFileName = "googleMapsV" + String.valueOf(_majorVersion);
			out.print("<script type=\"text/javascript\" src=\"");
			out.print(SystemData.get("path.js"));
			out.print('/');
			out.print(jsFileName);
			out.println(".js\"></script>");
			
			// Mark as added
			ContentHelper.addContent(pageContext, "JS", jsFileName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME);
		return EVAL_PAGE;
	}
}