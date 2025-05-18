// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import javax.servlet.jsp.*;

import org.json.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.JSONUtils;

/**
 * A JSP Tag to generate a GeoJSON LineString object.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class PointArrayTag extends MapEntryTag {

	private final Collection<GeoLocation> _entries = new ArrayList<GeoLocation>();

	/**
	 * Sets the points used to generate the array.
	 * @param points a Collection of GeoLocations
	 */
	public void setItems(Collection<GeoLocation> points) {
		_entries.addAll(points);
	}
	
	@Override
	public void release() {
		super.release();
		_entries.clear();
	}

	@Override
	public int doEndTag() throws JspException {
		
		// Create the core object
		JSONObject jo = new JSONObject();
		jo.put("type", "geojson");
		
		// Create the feature
		JSONObject fo = new JSONObject();
		fo.put("type", "Feature");
		fo.put("properties", new JSONObject());
		
		// Create the geometry
		JSONObject go = new JSONObject();
		JSONArray ca = new JSONArray();
		go.put("type", "LineString");
		_entries.forEach(pt -> ca.put(JSONUtils.toLL(pt)));
		go.put("coordinates", ca);
		fo.put("geometry", go);
		jo.put("data", fo);
		
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print(jo.toString());
			out.println(';');
			ContentHelper.addContent(pageContext, InsertAPITag.API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}