// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.util.*;

import com.flightaware.directflight.soap.DirectFlight.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.util.cache.*;

/**
 * Loads data from FlightAware via SOAP. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class GetFlightAware extends DAO implements CachingDAO {
	
	private static final Cache<CacheableList<FlightRoute>> _cache = 
		new ExpiringCache<CacheableList<FlightRoute>>(256, 86400);
	
	public int getHits() {
		return _cache.getHits();
	}
	
	public int getRequests() {
		return _cache.getRequests();
	}

	/**
	 * Retrieves routes between two airports.
	 * @param aD the origin Airport
	 * @param aA the destination Airport
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<FlightRoute> getRouteData(Airport aD, Airport aA) throws DAOException {

		// Check the cache
		String key = aD.getICAO() + "-" + aA.getICAO() ;
		CacheableList<FlightRoute> results = _cache.get(key);
		if (results != null)
			return results;
		
		try {
			results = new CacheableList<FlightRoute>(key);
			
			// Do the SOAP call
            DirectFlightLocator locator = new DirectFlightLocator();
            DirectFlightSoap df = locator.getDirectFlightSoap();
            RoutesBetweenAirportsStruct[] data = df.routesBetweenAirports(aD.getICAO(), aA.getICAO());
            
            // Loop through the results
            for (int x = 0; (data != null) && (x < data.length); x++) {
            	RoutesBetweenAirportsStruct r = data[x];
            	FlightRoute rt = new FlightRoute();
            	rt.setAirportD(aD);
            	rt.setAirportA(aA);
            	rt.setCreatedOn(new Date());
            	rt.setCruiseAltitude(r.getFiledAltitude().toString());
            	rt.setRoute(r.getRoute());
            	results.add(rt);
            }

            _cache.add(results);
            return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}