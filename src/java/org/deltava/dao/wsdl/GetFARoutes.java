// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.flightaware.directflight.soap.DirectFlight.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * Loads route data from FlightAware via SOAP. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class GetFARoutes extends FlightAwareDAO implements CachingDAO {
	
	private static final Logger log = Logger.getLogger(GetFARoutes.class);
	
	private static final FileSystemCache _cache =  new FileSystemCache(128, SystemData.get("schedule.cache"));
	
	public int getHits() {
		return _cache.getHits();
	}
	
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Loads routes from a filesystem cache.
	 */
	private Collection<ExternalFlightRoute> get(Airport aD, Airport aA) {
		String key = aD.getICAO() + "-" + aA.getICAO();
		CacheableFile f = _cache.get(key);
		if (f == null)
			return null;
		
		// Load the data
		Collection<ExternalFlightRoute> results = new ArrayList<ExternalFlightRoute>();
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			
			// Get the counts
			int rtCount = StringUtils.parse(p.getProperty("count"), 0);
			for (int x = 1; x <= rtCount; x++) {
				ExternalFlightRoute rt = new ExternalFlightRoute();
				rt.setAirportD(aD);
				rt.setAirportA(aA);
				rt.setID(x);
				rt.setSource(p.getProperty("route" + x + ".src"));
				rt.setSID(p.getProperty("route" + x + ".sid"));
				rt.setSTAR(p.getProperty("route" + x + ".star"));
				rt.setRoute(p.getProperty("route" + x + ".route"));
				rt.setCruiseAltitude(p.getProperty("route" + x + ".alt", "350"));
				rt.setCreatedOn(new Date(Long.parseLong(p.getProperty("route" + x + ".created"))));
				rt.setComments(p.getProperty("route" + x + ".comments"));
				results.add(rt);
			}
		} catch (IOException ie) {
			log.error("Cannot load cached routes - " + ie.getMessage(), ie);
		}
		
		return results;
	}
	
	/**
	 * Saves routes in a filesystem cache.
	 */
	private void save(Airport aD, Airport aA, Collection<ExternalFlightRoute> routes) {
		if (routes.isEmpty()) return;
		
		try {
			File f = File.createTempFile("faRoutes", aD.getICAO() + "$" + aA.getICAO());
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.println("count=" + routes.size());
			
			// Write the routes
			int ofs = 0;
			for (ExternalFlightRoute rt : routes) {
				ofs++;
				pw.println("route" + ofs + ".src=" + rt.getSource());
				pw.println("route" + ofs + ".created=" + String.valueOf(rt.getCreatedOn().getTime()));
				pw.println("route" + ofs + ".alt=" + rt.getCruiseAltitude());
				pw.println("route" + ofs + ".route=" + rt.getRoute());
				pw.println("route" + ofs + ".sid=" + rt.getSID());
				pw.println("route" + ofs + ".star=" + rt.getSTAR());
				pw.println("route" + ofs + ".comments=" + rt.getComments());
				pw.println();
			}
			
			// Close and cache
			pw.close();
			String key = aD.getICAO() + "-" + aA.getICAO();
			_cache.add(new CacheableFile(key, f));
		} catch (IOException ie) {
			log.error("Cannot cache routes - " + ie.getMessage(), ie);
		}
	}
	
	/**
	 * Retrieves routes between two airports.
	 * @param aD the origin Airport
	 * @param aA the destination Airport
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<ExternalFlightRoute> getRouteData(Airport aD, Airport aA) throws DAOException {

		// Check the cache
		Collection<ExternalFlightRoute> results = get(aD, aA);
		if (results != null)
			return results;
		
		results = new ArrayList<ExternalFlightRoute>();
		try {
			// Do the SOAP call
            RoutesBetweenAirportsStruct[] data = getStub().routesBetweenAirports(aD.getICAO(), aA.getICAO());
            
            // Loop through the results
            for (int x = 0; (data != null) && (x < data.length); x++) {
            	RoutesBetweenAirportsStruct r = data[x];
            	int altitude = r.getFiledAltitude().intValue();
            	ExternalFlightRoute rt = new ExternalFlightRoute();
            	rt.setID(x + 1);
            	rt.setAirportD(aD);
            	rt.setAirportA(aA);
            	rt.setCreatedOn(new Date());
            	rt.setCruiseAltitude((altitude < 1000) ? "FL" + String.valueOf(altitude) : String.valueOf(altitude));
            	rt.setSource("FlightAware");
            	rt.setComments("Loaded from FlightAware on " + rt.getCreatedOn());
            	
            	// Try and parse SID/STAR
            	List<String> waypoints = StringUtils.split(r.getRoute(), " "); 
            	try {
                	String[] wps = waypoints.toArray(new String[0]);
                	int wpMax = wps.length - 1;
                	boolean hasSID = (wpMax > 3) && (wps[0].length() > 4) && Character.isDigit(wps[0].charAt(wps[0].length() - 1));
                	if (hasSID) {
                		waypoints.remove(0);
                		rt.setSID(wps[0] + "." + wps[1] + ".ALL");
                	}

                	boolean hasSTAR = (wpMax > 3) && (wps[wpMax].length() > 4) && Character.isDigit(wps[wpMax].charAt(wps[wpMax].length() - 1));
                	if (hasSTAR) {
                		rt.setSTAR(wps[wpMax] + "." + wps[wpMax - 1] + ".ALL");
                		waypoints.remove(waypoints.size() - 1);
                	}
            	} finally {
            		rt.setRoute(StringUtils.listConcat(waypoints, " "));
            		results.add(rt);
            	}
            }

            save(aD, aA, results);
            return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}