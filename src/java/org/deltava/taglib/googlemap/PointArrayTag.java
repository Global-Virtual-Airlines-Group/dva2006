// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps GPoints.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PointArrayTag extends GoogleMapEntryTag {

	private Collection _entries;

	/**
	 * Sets the points used to generate the array.
	 * @param points a Collection of GeoLocations
	 */
	public void setItems(Collection points) {
		_entries = points;
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
			out.println(" = new Array();");

			// Create the markers
			for (Iterator i = _entries.iterator(); i.hasNext();) {
				GeoLocation entry = (GeoLocation) i.next();

				// Generate the google point and push it into the array
				out.print(_jsVarName);
				out.print(".push(new GPoint(");
				out.print(StringUtils.format(entry.getLongitude(), "##0.00000"));
				out.print(',');
				out.print(StringUtils.format(entry.getLatitude(), "##0.00000"));
				out.println("));");
			}
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the JavaScript variable as included
		ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);

		return EVAL_PAGE;
	}
}