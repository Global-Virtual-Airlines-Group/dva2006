// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to support Google Maps JSP tags.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public abstract class GoogleMapEntryTag extends TagSupport {

	/**
	 * Internal name used to check for Google Maps API inclusion.
	 */
	static final String API_JS_NAME = "$googleAPI$";

	/**
	 * The name of the Javascript variable to create.
	 */
	protected String _jsVarName;

	/**
	 * Sets the JavaScript variable to create.
	 * @param varName the variable name
	 */
	public void setVar(String varName) {
		_jsVarName = varName;
	}

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
		if (!ContentHelper.containsContent(pageContext, "JS", "googleMaps"))
			throw new IllegalStateException("googleMaps.js not included in request");

		return SKIP_BODY;
	}

	/**
	 * Resets the tag's state variables.
	 */
	public void release() {
		_jsVarName = null;
		super.release();
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
		StringBuilder buf = new StringBuilder("googleMarker(\'");
		buf.append(SystemData.get("path.img"));
		buf.append("\',\'");
		buf.append(color);
		buf.append("\',new GLatLng(");

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
		buf.append(",new GLatLng(");

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