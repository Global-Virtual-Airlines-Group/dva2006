// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.6
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
	
	public class FAFlightRoute extends FlightRoute implements ExternalFlightRoute {
		
		private String _source;
		
		public String getSource() {
			return _source;
		}
		
		public void setSource(String src) {
			_source = src;
		}
		
		public String getComboAlias() {
			return getRoute();
		}

		public String getComboName() {
			return toString();
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof FAFlightRoute))
				return false;
			
			try {
				FAFlightRoute r2 = (FAFlightRoute) o;
				return (getAirportD().equals(r2.getAirportD())) && (getAirportA().equals(r2.getAirportA())) &&
					toString().equals(r2.toString());
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	/**
	 * Loads routes from a filesystem cache.
	 */
	private Collection<FAFlightRoute> get(Airport aD, Airport aA) {
		String key = aD.getICAO() + "-" + aA.getICAO();
		CacheableFile f = _cache.get(key);
		if (f == null)
			return null;
		
		// Load the data
		Collection<FAFlightRoute> results = new LinkedHashSet<FAFlightRoute>();
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			
			// Get the counts
			int rtCount = StringUtils.parse(p.getProperty("count"), 0);
			for (int x = 1; x <= rtCount; x++) {
				FAFlightRoute rt = new FAFlightRoute();
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
	private void save(Airport aD, Airport aA, Collection<? extends FlightRoute> routes) {
		if (routes.isEmpty()) return;
		
		try {
			File f = File.createTempFile("faRoutes", aD.getICAO() + "$" + aA.getICAO());
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.println("count=" + routes.size());
			
			// Write the routes
			int ofs = 0;
			for (FlightRoute rt : routes) {
				ofs++;
				pw.println("route" + ofs + ".src=" + ((ExternalFlightRoute) rt).getSource());
				pw.println("route" + ofs + ".created=" + String.valueOf(rt.getCreatedOn().getTime()));
				pw.println("route" + ofs + ".alt=" + rt.getCruiseAltitude());
				pw.println("route" + ofs + ".route=" + rt.getRoute());
				if (rt.getSID() != null)
					pw.println("route" + ofs + ".sid=" + rt.getSID());
				if (rt.getSTAR() != null)
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
	public Collection<? extends FlightRoute> getRouteData(Airport aD, Airport aA) throws DAOException {

		// Check the cache
		Collection<FAFlightRoute> results = get(aD, aA);
		if (results != null)
			return results;
		
		results = new HashSet<FAFlightRoute>();
		List<FlightRoute> tmp = new ArrayList<FlightRoute>();
		try {
			// Do the SOAP call
            RoutesBetweenAirportsStruct[] data = getStub().routesBetweenAirports(aD.getICAO(), aA.getICAO());
            
            // Loop through the results
            for (int x = 0; (data != null) && (x < data.length); x++) {
            	RoutesBetweenAirportsStruct r = data[x];
            	int altitude = r.getFiledAltitude().intValue();
            	FAFlightRoute rt = new FAFlightRoute();
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
            		tmp.add(rt);
            	}
            }

            save(aD, aA, results);
            return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}