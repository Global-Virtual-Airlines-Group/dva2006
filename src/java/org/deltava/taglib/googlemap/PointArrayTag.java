// Copyright 2005, 2006, 2009, 2010, 2013, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.json.JSONArray;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.JSONUtils;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps Lat/Lon objects.
 * @author Luke
 * @version 7.3
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
	@Override
	public void release() {
		super.release();
		_entries.clear();
	}

	/**
	 * Renders the tag data to the JSP output stream, generating a JavaScript array with a number of Google Maps points.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if a network error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();

			// Create the markers
			JSONArray ja = new JSONArray();
			_entries.forEach(loc -> ja.put(JSONUtils.format(loc)));
			out.print(ja.toString());
			out.print(';');
			ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}