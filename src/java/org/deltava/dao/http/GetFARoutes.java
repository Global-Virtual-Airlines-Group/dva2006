// Copyright 2008, 2009, 2010, 2012, 2014, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;
import java.time.Instant;
import java.io.InputStream;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * Loads route data from FlightAware via SOAP. 
 * @author Luke
 * @version 8.6
 * @since 2.2
 */

public class GetFARoutes extends FlightAwareDAO {
	
	/**
	 * Retrieves routes between two Aairports.
	 * @param rp the RoutePair
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<ExternalRoute> getRouteData(RoutePair rp) throws DAOException {
		Collection<ExternalRoute> results = new LinkedHashSet<ExternalRoute>();
		
		// Build the URL
		Map<String, String> params = new HashMap<String, String>();
		params.put("origin", rp.getAirportD().getICAO());
		params.put("destination", rp.getAirportA().getICAO());
		
		try {
			init(buildURL("RoutesBetweenAirports", params)); JSONObject jo = null;
			try (InputStream is = getIn()) {
				jo = new JSONObject(new JSONTokener(is));
			}

			JSONObject ro = jo.optJSONObject("RoutesBetweenAirportsResult");
			if (ro == null)
				return results;
            
            // Loop through the results
			JSONArray data = ro.getJSONArray("data");
            for (int x = 0; x < data.length(); x++) {
            	JSONObject dto = data.getJSONObject(x);
            	int altitude = dto.optInt("filed_altitude_max");
            	ExternalRoute rt = new ExternalRoute("FlightAware");
            	rt.setAirportD(rp.getAirportD());
            	rt.setAirportA(rp.getAirportA());
            	rt.setCreatedOn(Instant.now());
            	rt.setCount(dto.optInt("count"));
            	rt.setCruiseAltitude((altitude < 1000) ? "FL" + String.valueOf(altitude) : String.valueOf(altitude));
            	rt.setComments("Loaded from FlightAware on " + rt.getCreatedOn());
            	
            	// Try and parse SID/STAR
            	List<String> waypoints = StringUtils.split(dto.optString("route"), " "); 
            	try {
                	String[] wps = waypoints.toArray(new String[0]);
                	int wpMax = wps.length - 1;
                	boolean hasSID = (wpMax > 1) && (wps[0].length() > 3) && Character.isDigit(wps[0].charAt(wps[0].length() - 1));
                	if (hasSID) {
                		waypoints.remove(wps[0]);
                		rt.setSID(wps[0] + "." + wps[1]);
                	}

                	boolean hasSTAR = (wpMax > 1) && (wps[wpMax].length() > 3) && Character.isDigit(wps[wpMax].charAt(wps[wpMax].length() - 1));
                	if (hasSTAR) {
                		rt.setSTAR(wps[wpMax] + "." + wps[wpMax - 1]);
                		waypoints.remove(wps[wpMax]);
                	}
            	} finally {
            		rt.setRoute(StringUtils.listConcat(waypoints, " "));
            		rt.setID(results.size() + 1);
            		results.add(rt);
            	}
            }

            return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}