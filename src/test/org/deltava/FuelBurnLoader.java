// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class FuelBurnLoader extends TestCase {

	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";

	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(FuelBurnLoader.class);

		// Init SystemData
		SystemData.init();
		SystemData.add("airline.code", "DVA");

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());

		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetCountry cdao = new GetCountry(_c);
		cdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetFuelBurn() throws DAOException, SQLException {

		// Load ACARS flight IDs
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		try (PreparedStatement ps = _c.prepareStatement("SELECT ACARS_ID FROM ACARS_PIREPS WHERE (ACARS_ID<>0) AND (TOTAL_FUEL=0)")) {
			ps.setFetchSize(1000);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		}

		// Set prepared statement
		int totalFlights = 0;
		try (PreparedStatement ps = _c.prepareStatement("UPDATE ACARS_PIREPS SET TOTAL_FUEL=? WHERE (ACARS_ID=?)")) {
			GetACARSPositions acdao = new GetACARSPositions(_c);
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext();) {
				int id = i.next().intValue();
				totalFlights++;

				// Load the ACARS Data
				FlightInfo info = acdao.getInfo(id);
				if (info == null) {
					log.warn("Missing Flight ID - " + id);
					continue;
				}

				// Calculate fuel burn
				Collection<? extends RouteEntry> entries = acdao.getRouteEntries(id, info.getArchived());
				FuelUse use = FuelUse.validate(entries);

				// Update the data
				ps.setInt(1, Math.max(1, use.getTotalFuel()));
				ps.setInt(2, id);
				ps.executeUpdate();

				if ((totalFlights % 250) == 0) {
					log.info(totalFlights + " flights updated");
					_c.commit();
				}
			}
		}

		_c.commit();
		log.info(totalFlights + " flights updated");
	}
}