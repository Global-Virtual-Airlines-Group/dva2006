// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import javax.servlet.jsp.*;

import org.json.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.*;

/**
 * A JSP Tag to generate a GeoJSON LineString object.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class PointArrayTag extends MapEntryTag {

	private static final int GC_SEGMENT_SIZE = 30;
	private final List<GeoLocation> _entries = new ArrayList<GeoLocation>();

	/**
	 * Sets the points used to generate the array.
	 * @param points a Collection of GeoLocations
	 */
	public void setItems(Collection<GeoLocation> points) {
		_entries.addAll(points);
	}
	
	@Override
	public void release() {
		_entries.clear();
		super.release();
	}
	
	@Override
	public int doStartTag() {
		
		// Convert to Great Circle route
		if (_entries.size() > 1) {
			GeoLocation lastLoc = _entries.getFirst();
			Collection<GeoLocation> gcPts = new ArrayList<GeoLocation>();
			for (int x = 1; x < _entries.size(); x++) {
				GeoLocation loc = _entries.get(x);
				if (lastLoc.distanceTo(loc) > GC_SEGMENT_SIZE)
					gcPts.addAll(GeoUtils.greatCircle(lastLoc, loc, GC_SEGMENT_SIZE));
				else
					gcPts.add(loc);
				
				lastLoc = loc;
			}
			
			_entries.clear();
			_entries.addAll(gcPts);
		}
		
		return EVAL_BODY_INCLUDE;
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