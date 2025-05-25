// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2016, 2017, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.*;

/**
 * An abstract Web Service to store common map plotting code. 
 * @author Luke
 * @version 12.0
 * @since 2.3
 */

public abstract class MapPlotService extends WebService {

	/**
	 * Converts route points into a JSON object.
	 * @param points a List of MapEntry beans
	 * @return a JSONObject
	 */
	protected static JSONObject formatPoints(List<NavigationDataBean> points) {

		// Calculate the distance and midpoint by taking the first/last waypoints
		GeoLocation mp = null; 
		int distance = 500;
		if (points.size() > 1) {
			NavigationDataBean ndf = points.getFirst();
			mp = ndf.getPosition().midPoint(points.getLast());
			distance = ndf.getPosition().distanceTo(points.getLast());
		} else if (points.size() == 1)
			mp = points.getFirst();

		// Save the midpoint
		JSONObject jo = new JSONObject();
		if (mp != null) {
			JSONObject mpo = new JSONObject();
			mpo.put("ll", JSONUtils.format(mp));
			mpo.put("distance", distance);
			jo.put("midPoint", mpo);
		}

		// Write the entries
		GeoLocation start = points.isEmpty() ? null : points.getFirst(); distance = 0;
		for (NavigationDataBean entry : points) {
			distance += entry.distanceTo(start);
			JSONObject po = new JSONObject();
			po.put("code", entry.getCode());
			po.put("ll", JSONUtils.format(entry));
			po.put("color", entry.getIconColor());
			po.put("info", entry.getInfoBox());
			po.put("pal", entry.getPaletteCode());
			po.put("icon", entry.getIconCode());
			start = entry;
			jo.append("positions", po);
		}
		
		// Plot GC route for MapBox
		List<GeoLocation> gcPts = GeoUtils.greatCircle(points);
		gcPts.forEach(pt -> jo.append("track", JSONUtils.format(pt)));
		JSONUtils.ensureArrayPresent(jo, "positions", "track");
		jo.put("distance", distance);
		return jo;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}
}