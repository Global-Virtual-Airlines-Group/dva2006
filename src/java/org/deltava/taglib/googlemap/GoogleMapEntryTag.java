// Copyright 2005, 2006, 2008, 2009, 2010, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to support Google Maps JSP tags.
 * @author Luke
 * @version 5.1
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
		
		// Get the API version
		Integer rawVersion = (Integer) pageContext.getAttribute(InsertGoogleAPITag.API_VER_ATTR_NAME, PageContext.REQUEST_SCOPE);
		_apiVersion = (rawVersion == null) ? 3 : rawVersion.intValue();

		// Check for Google Maps support JavaScript
		String jsFileName = (_apiVersion == 3) ? "googleMapsV3" : "googleMapsV2"; 
		if (!ContentHelper.containsContent(pageContext, "JS", jsFileName))
			throw new IllegalStateException("googleMaps.js not included in request");
		
		return SKIP_BODY;
	}
	
	/**
	 * Returns the Google Maps API version used on this page.
	 * @return the API major version
	 */
	protected int getAPIVersion() {
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
		StringBuilder buf = new StringBuilder("googleMarker(");
		if (_apiVersion < 3) {
			buf.append("\'");
			buf.append(SystemData.get("path.img"));
			buf.append("\',\'");
			buf.append(color);
			buf.append("\',new GLatLng(");
		} else {
			buf.append('\'');
			buf.append(color);
			buf.append("\',new google.maps.LatLng(");
		}

		// Format latitude/longitude
		buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
		buf.append("),");

		// Assign a label if one provided
		if (label != null) {
			buf.append('\'');
			buf.append(StringUtils.escapeSlashes(label));
			buf.append('\'');
		} else
			buf.append("null");

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
	protected String generateIconMarker(GeoLocation loc, int paletteCode, int iconCode, String label) {

		// Build the JS call
		StringBuilder buf = new StringBuilder("googleIconMarker(");
		buf.append(paletteCode);
		buf.append(',');
		buf.append(iconCode);
		buf.append(",new ");
		buf.append((_apiVersion == 3) ? "google.maps.LatLng(" : "GLatLng(");

		// Format latitude/longitude
		buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
		buf.append("),");
		
		// Assign a label if one provided
		if (label != null) {
			buf.append('\'');
			buf.append(StringUtils.escapeSlashes(label));
			buf.append('\'');
		} else
			buf.append("null");

		buf.append(')');
		return buf.toString();
	}
}