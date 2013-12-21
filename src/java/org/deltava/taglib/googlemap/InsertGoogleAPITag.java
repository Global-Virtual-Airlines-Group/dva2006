// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.json.JSONObject;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {

	static final String USAGE_ATTR_NAME = "$googleMapUsage$";
	static final String API_VER_ATTR_NAME = "$googleMapAPIVersion$";
	
	private static final int MIN_API_VERSION = 3;
	private static final String DEFAULT_V3_MINOR = "15";
	
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
	@Override
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
	@Override
	public int doStartTag() throws JspException {
		long value = USAGE_COUNT.incrementAndGet();
		if (value == 1)
			pageContext.setAttribute(USAGE_ATTR_NAME, USAGE_COUNT, PageContext.APPLICATION_SCOPE);
		
		// Translate stable/release v3 to minor version
		if ((_majorVersion == 3) && (_minorVersion == null))
			_minorVersion = DEFAULT_V3_MINOR;
		
		return super.doStartTag();
	}

	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	@Override
	public int doEndTag() throws JspException {

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;
		
		// Insert the API version
		pageContext.setAttribute(API_VER_ATTR_NAME, Integer.valueOf(_majorVersion), PageContext.REQUEST_SCOPE);
		
		JspWriter out = pageContext.getOut();
		try {
			out.print("<script src=\"");
			out.print((_majorVersion == 3) ? V3_API_URL : V3_API_URL);
			out.print(String.valueOf(_majorVersion));
			if (_minorVersion != null) {
				out.print('.');
				out.print(_minorVersion);
			}

			if (!_libraries.isEmpty()) {
				out.print("&amp;libraries=");
				for (Iterator<String> i = _libraries.iterator(); i.hasNext(); ) {
					out.print(i.next());
					if (i.hasNext())
						out.print(',');
				}
			}
			
			out.println("\"></script>");
			
			// Build the Map context object
			JSONObject mco = new JSONObject();
			mco.put("IMG_PATH", SystemData.get("path.img"));
			mco.put("API", _majorVersion);
			mco.put("tileHost", SystemData.get("weather.tileHost"));
			mco.put("multiHost", SystemData.getBoolean("weather.multiHost"));
			mco.put("seriesData", Collections.emptyMap());
			
			// Init common code
			out.println("<script>");
			out.print("golgotha.maps = ");
			out.print(mco.toString());
			out.println(";</script>");
			
			// Add JS support file
			String jsFileName = "googleMapsV" + String.valueOf(_majorVersion);
			out.print("<script src=\"");
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