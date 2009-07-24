// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.ACARSFlightReport;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;

import org.deltava.util.GeoUtils;
import org.deltava.util.system.SystemData;

public class RunwayLoader extends TestCase {
	
	private static Logger log;

	//private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";
	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/dva";
	private static final String JDBC_URL2 = "jdbc:mysql://polaris.sce.net/dva";

	private Connection _c;
	private Connection _c2;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(RunwayLoader.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "14072");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		_c2 = DriverManager.getConnection(JDBC_URL2, "luke", "14072");
		assertNotNull(_c2);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c2);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c2);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c2);
		SystemData.add("airlines", aldao.getAll());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetRunways() throws Exception {
		
		// Get the flight IDs
		log.info("Loading Flights");
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		Statement s = _c.createStatement();
		s.setFetchSize(3000);
		ResultSet rs = s.executeQuery("SELECT DISTINCT ID FROM acars.IDS ORDER BY ID");
		while (rs.next())
			IDs.add(new Integer(rs.getInt(1)));
		
		rs.close();
		s.close();
		
		// Iterate through the flights
		GetNavRoute navdao = new GetNavRoute(_c2);
		GetACARSData addao = new GetACARSData(_c);
		GetFlightReports frdao = new GetFlightReports(_c); 
		SetACARSData awdao = new SetACARSData(_c);
		log.info("Processing " + IDs.size() + " flights");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			int id = i.next().intValue();
			FlightInfo info = addao.getInfo(id);
			if (info == null)
				continue;
			
			// Get the runway data
			List<RouteEntry> tdEntries = addao.getTakeoffLanding(info.getID(), info.getArchived());
			if (tdEntries.size() > 2) {
				int ofs = 0; RouteEntry entry = tdEntries.get(0);
				GeoPosition adPos = new GeoPosition(info.getAirportD());
				while ((ofs < (tdEntries.size() - 1)) && (adPos.distanceTo(entry) < 15) && (entry.getVerticalSpeed() > 0)) {
					ofs++;
					entry = tdEntries.get(ofs);
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
			ACARSFlightReport afr = frdao.getACARS("dva", id);
			if (afr == null)
				afr = frdao.getACARS("afv", id);
			if (afr == null) {
				log.warn("Cannot find PIREP for " + id + ", skipping");
				continue;
			}
			
			// Load the runways
			boolean newData = false;
			Runway rwyD = navdao.getBestRunway(info.getAirportD().getICAO(), afr.getFSVersion(), tdEntries.get(0), tdEntries.get(0).getHeading());
			if ((rwyD != null) && ((info.getRunwayD() == null) || (!rwyD.getCode().equals(info.getRunwayD().getCode())))) {
				if (info.getRunwayD() != null)
					log.warn("For Flight " + info.getID() + " runwayD was " + info.getRunwayD().getCode() + ", now" + rwyD.getCode());
				
				int dist = GeoUtils.distanceFeet(rwyD, tdEntries.get(0));
				if (dist < 65520) {
					info.setRunwayD(new RunwayDistance(rwyD, dist));
					newData = true;
				}
			}
			
			Runway rwyA = navdao.getBestRunway(afr.getAirportA().getICAO(), afr.getFSVersion(), tdEntries.get(1), tdEntries.get(1).getHeading());
			if ((rwyA != null) && ((info.getRunwayA() == null) || (!rwyA.getCode().equals(info.getRunwayA().getCode())))) {
				if (info.getRunwayA() != null)
					log.warn("For Flight " + info.getID() + " runwayA was " + info.getRunwayA().getCode() + ", now" + rwyA.getCode());
					
				int dist = GeoUtils.distanceFeet(rwyA, tdEntries.get(1));
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