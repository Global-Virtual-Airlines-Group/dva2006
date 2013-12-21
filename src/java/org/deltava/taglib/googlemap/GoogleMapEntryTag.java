// Copyright 2005, 2006, 2008, 2009, 2010, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.json.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.*;

import org.deltava.util.StringUtils;

/**
 * An abstract class to support Google Maps JSP tags.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

abstract class GoogleMapEntryTag extends JSTag {

	/**
	 * Internal name used to check for Google Maps API inclusion.
	 */
	static final String API_JS_NAME = "$googleAPI$";
	
	private int _apiVersion;

	/**
	 * Executed before the Tag is rendered. This will check for the presence of required JavaScript files in the
	 * request. Tags that do not require this check can override this method.
	 * @return TagSupport.SKIP_BODY always
	 * @throws IllegalStateException if the Google Maps API or googleMaps.js not included in request
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Check for Google Maps API
		if (!ContentHelper.containsContent(pageContext, "JS", API_JS_NAME))
			throw new IllegalStateException("Google Maps API not included in request");
		
		// Check for Google Maps support JavaScript
		String jsFileName = (getAPIVersion() == 4) ? "googleMapsV4" : "googleMapsV3"; 
		if (!ContentHelper.containsContent(pageContext, "JS", jsFileName))
			throw new IllegalStateException(jsFileName + ".js not included in request");
		
		return SKIP_BODY;
	}
	
	/**
	 * Returns the Google Maps API version used on this page.
	 * @return the API major version
	 */
	protected int getAPIVersion() {
		if (_apiVersion == 0) {
			Integer rawVersion = (Integer) pageContext.getAttribute(InsertGoogleAPITag.API_VER_ATTR_NAME, PageContext.REQUEST_SCOPE);
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
	protected String generateMarker(GeoLocation loc, String color, String label) {

		// Build the JS call
		JSONObject jo = new JSONObject();
		try {
			jo.put("color", color);
			if (label != null)
				jo.put("info", label);
		} catch (JSONException je) {
			// empty
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append("new golgotha.maps.Marker(");
		buf.append(jo.toString());
		buf.append(", new google.maps.LatLng(");

		// Format latitude/longitude
		buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
		buf.append("))");
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
	protected String generateIconMarker(GeoLocation loc, int paletteCode, int iconCode, String label) {

		// Build the options
		JSONObject jo = new JSONObject();
		try {
			jo.put("pal", paletteCode);
			jo.put("icon", iconCode);
			if (label != null)
				jo.put("info", label);
		} catch (JSONException je) {
			// empty
		}

		// Build the JS call
		StringBuilder buf = new StringBuilder("new golgotha.maps.IconMarker(");
		buf.append(jo.toString());
		buf.append(",new google.maps.LatLng(");

		// Format latitude/longitude
		buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
		buf.append("))");
		return buf.toString();
	}
}