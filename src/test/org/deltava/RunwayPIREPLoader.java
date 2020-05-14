// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class RunwayPIREPLoader extends TestCase {
	
	private static Logger log;

	//private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";
	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/afv";
	
	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(RunwayPIREPLoader.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("all")
	public void testSetRunways() throws Exception {
		
		// Load the PIREPs
		Map<Integer, Integer> IDs = new LinkedHashMap<Integer, Integer>();
		try (Statement s = _c.createStatement()) {
			s.setFetchSize(2000);
			try (ResultSet rs = s.executeQuery("SELECT ID, ACARS_ID FROM IDS WHERE (DONE=0)")) {
				while (rs.next())
					IDs.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			}
		}
		
		// Build the update statements
		int flightsDone = 0;
		GetACARSData fddao = new GetACARSData(_c);
		GetFlightReportACARS frdao = new GetFlightReportACARS(_c);
		try (PreparedStatement ps = _c.prepareStatement("UPDATE ACARS_PIREPS SET TAKEOFF_LAT=?, TAKEOFF_LNG=?,"
			+ "TAKEOFF_ALT=?, TAKEOFF_HDG=?, LANDING_LAT=?, LANDING_LNG=?, LANDING_ALT=?, LANDING_HDG=? WHERE (ID=?)")) {
			for (Map.Entry<Integer, Integer> me : IDs.entrySet()) {
				FlightInfo info = fddao.getInfo(me.getValue().intValue());
				if (info == null) {
					log.warn("Flight " + me.getValue() + " not found!");
					continue;
				}
				
				List<? extends RouteEntry> tdEntries = fddao.getTakeoffLanding(info.getID()); // won't work with serialized archive
				if (tdEntries.size() > 2) {
					int ofs = 0;
					ACARSRouteEntry entry = (ACARSRouteEntry) tdEntries.get(0);
					GeoPosition adPos = new GeoPosition(info.getAirportD());
					while ((ofs < (tdEntries.size() - 1)) && (adPos.distanceTo(entry) < 15) && (entry.getVerticalSpeed() > 0)) {
						ofs++;
						entry = (ACARSRouteEntry) tdEntries.get(ofs);
					}

					// Trim out spurious takeoff entries
					if (ofs > 0)
						tdEntries.subList(0, ofs - 1).clear();
					if (tdEntries.size() > 2)
						tdEntries.subList(1, tdEntries.size() - 1).clear();
				}
				
				// Save the entry points
				if (tdEntries.size() > 0) {
					RouteEntry re = tdEntries.get(0);
					if (re.getAltitude() > 21000) {
						log.warn("Takeoff altitude = " + re.getAltitude());
						continue;
					}
					
					ps.setDouble(1, re.getLatitude());
					ps.setDouble(2, re.getLongitude());
					ps.setInt(3, re.getAltitude());
					ps.setInt(4, re.getHeading());
					
					if (tdEntries.size() > 1) {
						re = tdEntries.get(1);
						if (re.getAltitude() > 21000) {
							log.warn("Landing altitude = " + re.getAltitude());
							continue;
						}
						
						ps.setDouble(5, re.getLatitude());
						ps.setDouble(6, re.getLongitude());
						ps.setInt(7, re.getAltitude());
						ps.setInt(8, re.getHeading());
					} else {
						ps.setDouble(5, 0);
						ps.setDouble(6, 0);
						ps.setInt(7, 0);
						ps.setInt(8, -1);
					}
					
					ps.setInt(9, me.getKey().intValue());
					ps.executeUpdate();
					try (PreparedStatement ps2 = _c.prepareStatement("UPDATE IDS SET DONE=1 WHERE (ID=?)")) {
						ps2.setInt(1, me.getKey().intValue());
						ps2.executeUpdate();
					}
				} else
					log.debug("Cannot update takeoff/touchdown - " + tdEntries.size() + " touchdown points");

				flightsDone++;
				if ((flightsDone % 100) == 0) {
					log.info(flightsDone + " flights updated");
					_c.commit();
				}
			}
		}
		
		_c.commit();
	}
}