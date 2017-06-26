// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.JSONUtils;

/**
 * An abstract Web Service to store common map plotting code. 
 * @author Luke
 * @version 7.5
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
			NavigationDataBean ndf = points.get(0);
			mp = ndf.getPosition().midPoint(points.get(points.size() - 1));
			distance = ndf.getPosition().distanceTo(points.get(points.size() - 1));
		} else if (points.size() == 1)
			mp = points.get(0);

		// Save the midpoint
		JSONObject jo = new JSONObject();
		if (mp != null) {
			JSONObject mpo = new JSONObject();
			mpo.put("ll", JSONUtils.format(mp));
			mpo.put("distance", distance);
			jo.put("midPoint", mpo);
		}

		// Write the entries
		GeoLocation start = points.isEmpty() ? null : points.get(0); distance = 0;
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

		JSONUtils.ensureArrayPresent(jo, "positions");
		jo.put("distance", distance);
		return jo;
	}
	
	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}
}