// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a Google Maps v2 Lat/Long point.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PointTag extends GoogleMapEntryTag {

	private GeoLocation _entry;

	/**
	 * Sets the location of the marker.
	 * @param loc the marker's location
	 */
	public void setPoint(GeoLocation loc) {
		_entry = loc;
	}

	/**
	 * Renders the JSP tag, creating a Javascript GPoint.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException
	 */
	public int doEndTag() throws JspException {

		JspWriter out = pageContext.getOut();
		try {
			// Assign to a variable if a name was provided, otherwise make an anonymous object
			if (_jsVarName != null) {
				out.print("var ");
				out.print(_jsVarName);
				out.print(" = ");
			}

			// Create the GPoint
			out.print("new GLatLng(");
			out.print(StringUtils.format(_entry.getLatitude(), "##0.00000"));
			out.print(',');
			out.print(StringUtils.format(_entry.getLongitude(), "##0.00000"));
			out.print(");");
			
			// Mark the JavaScript marker variable as included
			if (_jsVarName != null)
				ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}