// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.File;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FDRFlightReport;
import org.deltava.beans.navdata.Gate;
import org.deltava.comparators.GeoComparator;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class GatePIREPLoader extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/acars";
	
	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(GatePIREPLoader.class);
		
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
		super.tearDown();
	}

	public void testSetGates() throws Exception {
		
		// Load the PIREPs
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		try (Statement s = _c.createStatement()) {
			s.setFetchSize(2000);
			try (ResultSet rs = s.executeQuery("SELECT ID FROM acars.FLIGHTS WHERE (PIREP=1) AND (ID>1478)")) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		}
		
		// Remove already loaded flights
		try (Statement s = _c.createStatement()) {
			s.setFetchSize(2000);
			try (ResultSet rs = s.executeQuery("SELECT DISTINCT ID FROM acars.GATEDATA")) {
				while (rs.next())
					IDs.remove(Integer.valueOf(rs.getInt(1)));
			}
		}

		int flightsDone = 0;
		GetUserData uddao = new GetUserData(_c);
		GetFlightReports frdao = new GetFlightReports(_c);
		GetGates gdao = new GetGates(_c);
		GetACARSPositions ardao = new GetACARSPositions(_c);
		SetACARSRunway gwdao = new SetACARSRunway(_c);
		for (Integer i : IDs) {
			FlightInfo fi = ardao.getInfo(i.intValue());
			List<? extends GeoLocation> entries = ardao.getRouteEntries(fi.getID(), false, fi.getArchived());
			if (entries.size() < 4) continue;
			
			// Load the user and the PIREP
			UserData ud = uddao.get(fi.getAuthorID());
			FDRFlightReport fr = frdao.getACARS(ud.getDB(), fi.getID());
			if (fr == null) {
				log.warn("Flight " + fi.getID() + " - " + ud.getDB().toUpperCase() + " has no PIREP!");
				continue;
			}
			
			boolean isUpdated = false;
			GeoComparator dgc = new GeoComparator(entries.get(0), true);
			GeoComparator agc = new GeoComparator(entries.get(entries.size() - 1), true);
			
			// Get the closest departure gate
			SortedSet<Gate> dGates = new TreeSet<Gate>(dgc);
			dGates.addAll(gdao.getGates(fr.getAirportD()));
			if (!dGates.isEmpty()) {
				Gate g = dGates.first();
				int dist = g.distanceFeet(dgc.getLocation());
				if (dist < 2250) {
					fi.setGateD(g);
					isUpdated = true;
				}
				
				if (dist > 1000)
					log.info(g.getName() + " at " + g.getCode() + " is " + dist + "ft from first point - " + fi.getID());
			}
			
			// Get the closest arrival gate
			SortedSet<Gate> aGates = new TreeSet<Gate>(agc);
			aGates.addAll(gdao.getGates(fr.getAirportA()));
			if (!aGates.isEmpty()) {
				Gate g = aGates.first();
				int dist = g.distanceFeet(agc.getLocation());
				if (dist < 2250) {
					fi.setGateA(g);
					isUpdated = true;
				}
				
				if (dist > 1000)
					log.info(g.getName() + " at " + g.getCode() + " is " + dist + "ft from last point - " + fi.getID());
			}
			
			// Write gates
			if (isUpdated) {
				gwdao.writeGates(fi);
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