// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

public class LoadHistoric extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL ="jdbc:mysql://pollux.gvagroup.org/dva";
	private final DateFormat TF = new SimpleDateFormat("HHmm");
	
	private Connection _c;
	private final Map<String, Airline> _airlines = new HashMap<String, Airline>();
	private final Map<String, Airport> _airports = new HashMap<String, Airport>();

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(LoadHistoric.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		
		// Load Time Zones, Airlines and Airports
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirline aldao = new GetAirline(_c);
		_airlines.putAll(aldao.getAll());
		GetAirport apdao = new GetAirport(_c);
		_airports.putAll(apdao.getAll());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadHistoric() throws Exception {
		
		File f = new File("u:\\luke\\dva\\schedule\\DELTA_MARCH_1973.csv");
		assertTrue(f.exists());
		
		// Load the file
		SetSchedule swdao = new SetSchedule(_c);
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		while (lr.ready()) {
			String data = lr.readLine();
			StringTokenizer tkns = new StringTokenizer(data, ",");
			if (data.startsWith(";"))
				continue;
			
			// Build the flight
			Airline a = _airlines.get("DVH");
			int flight = Integer.parseInt(tkns.nextToken());
			int leg = Integer.parseInt(tkns.nextToken());
			ScheduleEntry se = new ScheduleEntry(a, flight, leg);
			
			// Get airports and time
			String adCode = tkns.nextToken().toUpperCase();
			Airport ad = _airports.get(adCode);
			if (ad == null) log.warn("Unknown Airport - " + adCode);
			assertNotNull(ad);
			se.setAirportD(ad);
			String timeD = tkns.nextToken();
			String aaCode = tkns.nextToken().toUpperCase();
			Airport aa = _airports.get(aaCode);
			if (aa == null) log.warn("Unknown Airport - " + aaCode);
			assertNotNull(aa);
			se.setAirportA(aa);
			String timeA = tkns.nextToken();
			se.setEquipmentType(tkns.nextToken());
			
			// Strip off the time zones from the airports
			se.setTimeD(TF.parse(timeD.substring(0, timeD.indexOf(' '))));
			se.setTimeA(TF.parse(timeA.substring(0, timeA.indexOf(' '))));
			
			// Set flags
			se.setCanPurge(false);
			se.setHistoric(true);
			se.setAcademy(false);
			
			// Write the data
			swdao.write(se, false);
			log.info("Saved " + se);
		}
		
		_c.commit();
	}
}