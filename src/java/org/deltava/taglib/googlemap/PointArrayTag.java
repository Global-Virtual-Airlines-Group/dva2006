// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps GPoints.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class PointArrayTag extends GoogleMapEntryTag {

	private final Collection<GeoLocation> _entries = new ArrayList<GeoLocation>();

	/**
	 * Sets the points used to generate the array.
	 * @param points a Collection of GeoLocations
	 */
	public void setItems(Collection<GeoLocation> points) {
		_entries.addAll(points);
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_entries.clear();
	}

	/**
	 * Renders the tag data to the JSP output stream, generating a JavaScript array with a number of Google Maps points.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if a network error occurs
	 */
	public int doEndTag() throws JspException {

		// Create the JavaScript array definition
		JspWriter out = pageContext.getOut();
		try {
			out.print("var ");
			out.print(_jsVarName);
			out.println(" = [];");

			// Create the markers
			for (Iterator<GeoLocation> i = _entries.iterator(); i.hasNext();) {
				GeoLocation entry = i.next();

				// Generate the google point and push it into the array
				out.print(_jsVarName);
				out.print(".push(new GLatLng(");
				out.print(StringUtils.format(entry.getLatitude(), "##0.00000"));
				out.print(',');
				out.print(StringUtils.format(entry.getLongitude(), "##0.00000"));
				out.println("));");
			}
			
			// Mark the JavaScript variable as included
			ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}