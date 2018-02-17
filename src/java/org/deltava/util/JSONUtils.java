// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.json.*;

import java.time.*;

import org.deltava.beans.GeoLocation;

/**
 * A utility class for dealing with JSON objects. 
 * @author Luke
 * @version 8.2
 * @since 7.3
 */

public class JSONUtils {

	// static class
	private JSONUtils() {
		super();
	}

	/**
	 * Ensures a JSON object has certain array properties populated.
	 * @param o the JSONObject
	 * @param names the property names
	 */
	public static void ensureArrayPresent(JSONObject o, String...names) {
		for (int x = 0; x < names.length; x++) {
			String name = names[x];
			if (!o.has(name))
				o.put(name, new JSONArray());
		}
	}
	
	/**
	 * Converts a location into a Google Maps LatLngLiteral JSON object. 
	 * @param loc a GeoLocation
	 * @return a JSONObject
	 */
	public static JSONObject format(GeoLocation loc) {
		JSONObject jo = new JSONObject();
		jo.put("lat", loc.getLatitude());
		jo.put("lng", loc.getLongitude());
		return jo;
	}
	
	/**
	 * Converts an Instant into a JSON object with day/month/year components.
	 * @param dt the Instant
	 * @return a JSONobject
	 */
	public static JSONObject format(Instant dt) {
		LocalDateTime ldt = LocalDateTime.ofInstant(dt, ZoneOffset.UTC);
		JSONObject dto = new JSONObject();
		dto.put("y", ldt.getYear()); dto.put("m", ldt.getMonthValue()); dto.put("d", ldt.getDayOfMonth());
		return dto;
	}
}