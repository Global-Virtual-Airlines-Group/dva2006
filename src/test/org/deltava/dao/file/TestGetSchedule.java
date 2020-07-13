package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestGetSchedule extends TestCase {

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/afv?useSSL=false";
	
	private final Collection<Airline> _airlines = new LinkedHashSet<Airline>();
	private final Collection<Aircraft> _acTypes= new LinkedHashSet<Aircraft>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		PropertyConfigurator.configure("etc/log4j.test.properties");

		SystemData.init();

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "14072")) {
			assertNotNull(c);

			// Load the airports/time zones
			GetTimeZone tzdao = new GetTimeZone(c);
			tzdao.initAll();
			GetAirport apdao = new GetAirport(c);
			SystemData.add("airports", apdao.getAll());
			GetAirline aldao = new GetAirline(c);
			Map<String, Airline> airlines = aldao.getAll();
			_airlines.addAll(airlines.values());
			SystemData.add("airlines", airlines);

			// Get EQ types
			GetAircraft acdao = new GetAircraft(c);
			_acTypes.addAll(acdao.getAll());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoad() throws Exception {
		
		File f = new File("data", "afv_schedule.csv");
		assertTrue(f.exists());
		
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 16384)) {
			GetSchedule sdao = new GetSchedule(ScheduleSource.MANUAL, is);
			sdao.setAirlines(_airlines);
			sdao.setAircraft(_acTypes);
			
			Collection<RawScheduleEntry> results = sdao.process();
			assertNotNull(results);
			assertFalse(results.isEmpty());
			
			ImportStatus status = sdao.getStatus();
			assertNotNull(status);
			assertEquals(ScheduleSource.MANUAL, status.getSource());
		}
	}
}