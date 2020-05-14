// Copyright 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FDRFlightReport;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class RunwayLoader extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";
	private static final String JDBC_URL2 = JDBC_URL;

	private Connection _c;
	private Connection _c2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(RunwayLoader.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		_c2 = DriverManager.getConnection(JDBC_URL2, "luke", "test");
		assertNotNull(_c2);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c2);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c2);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c2);
		SystemData.add("airlines", aldao.getAll());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetRunways() throws Exception {
		
		// Get the flight IDs
		log.info("Loading Flights");
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		try (Statement s = _c.createStatement()) {
			s.setFetchSize(3000);
			try (ResultSet rs = s.executeQuery("SELECT DISTINCT ID FROM acars.IDS ORDER BY ID")) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		}
		
		// Iterate through the flights
		GetNavRoute navdao = new GetNavRoute(_c2);
		GetACARSData addao = new GetACARSData(_c);
		GetFlightReports frdao = new GetFlightReports(_c); 
		SetACARSRunway awdao = new SetACARSRunway(_c);
		log.info("Processing " + IDs.size() + " flights");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			int id = i.next().intValue();
			FlightInfo info = addao.getInfo(id);
			if (info == null)
				continue;
			
			// Get the runway data
			List<? extends RouteEntry> tdEntries = addao.getTakeoffLanding(info.getID()); // won't work with serialized archive
			if (tdEntries.size() > 2) {
				int ofs = 0; ACARSRouteEntry entry = (ACARSRouteEntry) tdEntries.get(0);
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
				
				assertEquals(2, tdEntries.size());
			}

			// If we don't have takeoff/touchdown data, fuggedaboutit
			if (tdEntries.size() != 2) {
				log.info(id + " has " + tdEntries.size() + " touchdown entries, skipping");
				continue;
			}
			
			// Get the PIREP
			FDRFlightReport afr = frdao.getACARS("dva", id);
			if (afr == null)
				afr = frdao.getACARS("afv", id);
			if (afr == null) {
				log.warn("Cannot find PIREP for " + id + ", skipping");
				continue;
			}
			
			// Load the runways
			boolean newData = false;
			LandingRunways lrD = navdao.getBestRunway(info.getAirportD(), afr.getSimulator(), tdEntries.get(0), tdEntries.get(0).getHeading());
			Runway rwyD = lrD.getBestRunway();
			if ((rwyD != null) && ((info.getRunwayD() == null) || (!rwyD.getCode().equals(info.getRunwayD().getCode())))) {
				if (info.getRunwayD() != null)
					log.warn("For Flight " + info.getID() + " runwayD was " + info.getRunwayD().getCode() + ", now" + rwyD.getCode());
				
				int dist = rwyD.distanceFeet(tdEntries.get(0));
				if (dist < 65520) {
					info.setRunwayD(new RunwayDistance(rwyD, dist));
					newData = true;
				}
			}
			
			LandingRunways lrA = navdao.getBestRunway(afr.getAirportA(), afr.getSimulator(), tdEntries.get(1), tdEntries.get(1).getHeading());
			Runway rwyA = lrA.getBestRunway();
			if ((rwyA != null) && ((info.getRunwayA() == null) || (!rwyA.getCode().equals(info.getRunwayA().getCode())))) {
				if (info.getRunwayA() != null)
					log.warn("For Flight " + info.getID() + " runwayA was " + info.getRunwayA().getCode() + ", now" + rwyA.getCode());
					
				int dist = rwyA.distanceFeet(tdEntries.get(1));
				if (dist < 65520) {
					info.setRunwayA(new RunwayDistance(rwyA, dist));
					newData = true;
				}
			}
				
			// Write the runways
			if (newData) {
				awdao.writeRunways(id, info.getRunwayD(), info.getRunwayA());
				_c.commit();
				if (id % 25 == 0)
					log.info("Wrote runways for " + id);
			}
		}
	}
}