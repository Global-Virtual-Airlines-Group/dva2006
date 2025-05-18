// Copyright 2005, 2006, 2008, 2009, 2010, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.json.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.*;

/**
 * An abstract class to support MapBox JSP tags.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

abstract class MapEntryTag extends JSTag {

	private int _apiVersion;

	/**
	 * Executed before the Tag is rendered. This will check for the presence of required JavaScript files in the request. Tags that do not require this check can override this method.
	 * @return TagSupport.SKIP_BODY always
	 * @throws IllegalStateException if the MapBox API or mapbox.js not included in request
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Check for Google Maps API
		if (!ContentHelper.containsContent(pageContext, "JS", InsertAPITag.API_JS_NAME))
			throw new IllegalStateException("Google Maps API not included in request");
		
		// Check for Google Maps support JavaScript
		String jsFileName = (getAPIVersion() == 4) ? "mapBoxV4" : "mapBoxV3"; 
		if (!ContentHelper.containsContent(pageContext, "JS", jsFileName))
			throw new IllegalStateException(jsFileName + ".js not included in request");
		
		return SKIP_BODY;
	}
	
	/**
	 * Returns the MapBox API version used on this page.
	 * @return the API major version
	 */
	protected int getAPIVersion() {
		if (_apiVersion == 0) {
			Integer rawVersion = (Integer) pageContext.getAttribute(InsertAPITag.API_VER_ATTR_NAME, PageContext.REQUEST_SCOPE);
			_apiVersion = (rawVersion == null) ? 3 : rawVersion.intValue();	
		}
		
		return _apiVersion;
	}

	/**
	 * Generates a call to googleMarker() to generate a Google Maps marker.
	 * @param loc the location
	 * @param color the icon color
	 * @param label the label HTML text, or null if none
	 * @return a JavaScript function call definition
	 */
	protected static String generateMarker(GeoLocation loc, String color, String label) {

		// Build the JS call
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(loc.getLongitude());
		ja.put(loc.getLatitude());
		jo.put("pt", ja);
		jo.put("color", color);
		if (label != null)
			jo.put("info", label);
		
		StringBuilder buf = new StringBuilder("new golgotha.maps.Marker(");
		buf.append(jo.toString());
		buf.append(')');
		return buf.toString();
	}
	
	/**
	 * Generates a call to googleIconMarker() to generate a Google Maps icon marker.
	 * @param loc the location
	 * @param paletteCode the Google Earth palette code
	 * @param iconCode the Google Earth icon code
	 * @param label the label HTML text, or null if none
	 * @return a JavaScript function call definition
	 */
	protected static String generateIconMarker(GeoLocation loc, int paletteCode, int iconCode, String label) {

		// Build the options
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(loc.getLongitude());
		ja.put(loc.getLatitude());
		jo.put("pt", ja);
		jo.put("pal", paletteCode);
		jo.put("icon", iconCode);
		if (label != null)
			jo.put("info", label);

		// Build the JS call
		StringBuilder buf = new StringBuilder("new golgotha.maps.IconMarker(");
		buf.append(jo.toString());
		buf.append(')');
		return buf.toString();
	}
}