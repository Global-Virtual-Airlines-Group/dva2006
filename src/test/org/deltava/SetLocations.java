// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.stats.GeocodeResult;

import org.deltava.dao.*;
import org.deltava.dao.http.GetGoogleGeocode;

public class SetLocations extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL ="jdbc:mysql://pollux.gvagroup.org/dva";
	
	private Connection _c;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(SetLocations.class);

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		_c.setAutoCommit(false);
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadLocations() throws Exception {
		
		// Get locations and Pilots
		GetPilot pdao = new GetPilot(_c);
		SetPilot pwdao = new SetPilot(_c);
		Map<Integer, GeoLocation> locs = pdao.getPilotBoard();
		
		// Loop through the locations
		for (Iterator<Map.Entry<Integer,GeoLocation>> i = locs.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Integer, GeoLocation> e = i.next();
			GeoLocation loc = e.getValue();
			
			// Read via the DAO
			try {
				GetGoogleGeocode gcdao = new GetGoogleGeocode();
				gcdao.setAPIKey("ABQIAAAAWFzTV_nG8JA7h9y7QsKTgRQs34dwJzNMJEKBtecbaszCuM2KJhQfgxuxzo6F3mVptlQ6PYPapCaeaA");
				List<GeocodeResult> locations = gcdao.getGeoData(loc.getLatitude(), loc.getLongitude());
				if (!locations.isEmpty()) {
					GeocodeResult gr = locations.get(0);
					if (gr.getAccuracy().intValue() > GeocodeResult.GeocodeAccuracy.COUNTRY.intValue()) {
						log.info("Setting " + e.getKey() + " home town to " + gr.getCityState());
						pwdao.setHomeTown(e.getKey().intValue(), gr);
					}
				}
			} catch (Exception ex) {
				log.error("Cannot update " + e.getKey() + " - " + ex.getMessage());
			}
		}
		
		// Commit
		_c.commit();
	}
}