// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a Mapbox LngLat literal.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class PointTag extends MapEntryTag {

	private GeoLocation _entry;

	/**
	 * Sets the location of the marker.
	 * @param loc the marker's location
	 */
	public void setPoint(GeoLocation loc) {
		_entry = loc;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print('[');
			out.print(StringUtils.format(_entry.getLongitude(), "##0.00000"));
			out.print(',');
			out.print(StringUtils.format(_entry.getLatitude(), "##0.00000"));
			out.println("];");
			
			// Mark the JavaScript marker variable as included
			if (_jsVarName != null)
				ContentHelper.addContent(pageContext, InsertAPITag.API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}