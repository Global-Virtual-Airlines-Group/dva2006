// Copyright 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.util.*;

import com.flightaware.flightxml.soap.FlightXML2.*;

import org.deltava.beans.schedule.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * Loads route data from FlightAware via SOAP. 
 * @author Luke
 * @version 3.4
 * @since 2.2
 */

public class GetFARoutes extends FlightAwareDAO {
	
	/**
	 * Retrieves routes between two airports.
	 * @param aD the origin Airport
	 * @param aA the destination Airport
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<? extends FlightRoute> getRouteData(Airport aD, Airport aA) throws DAOException {
		Collection<ExternalRoute> results = new LinkedHashSet<ExternalRoute>();
		try {
			// Do the SOAP call
			RoutesBetweenAirportsRequest req = new RoutesBetweenAirportsRequest(aD.getICAO(), aA.getICAO());
            RoutesBetweenAirportsResults rsp = getStub().routesBetweenAirports(req);
            RoutesBetweenAirportsStruct[] data = rsp.getRoutesBetweenAirportsResult();
            
            // Loop through the results
            for (int x = 0; (data != null) && (x < data.length); x++) {
            	RoutesBetweenAirportsStruct r = data[x];
            	int altitude = r.getFiledAltitude();
            	ExternalRoute rt = new ExternalRoute("FlightAware");
            	rt.setAirportD(aD);
            	rt.setAirportA(aA);
            	rt.setCreatedOn(new Date());
            	rt.setCruiseAltitude((altitude < 1000) ? "FL" + String.valueOf(altitude) : String.valueOf(altitude));
            	rt.setComments("Loaded from FlightAware on " + rt.getCreatedOn());
            	
            	// Try and parse SID/STAR
            	List<String> waypoints = StringUtils.split(r.getRoute(), " "); 
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