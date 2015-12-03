// Copyright 2005, 2006, 2010, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a Google LatLng literal.
 * @author Luke
 * @version 6.3
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
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print("{lat:");
			out.print(StringUtils.format(_entry.getLatitude(), "##0.00000"));
			out.print(",lng:");
			out.print(StringUtils.format(_entry.getLongitude(), "##0.00000"));
			out.print("};");
			
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