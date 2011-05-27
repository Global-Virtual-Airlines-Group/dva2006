// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.dao.*;

import junit.framework.TestCase;

public class FuelBurnLoader extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/dva";
	
	private Connection _c;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(RunwayLoader.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetFuelBurn() throws DAOException, SQLException {
		
		// Load ACARS flight IDs
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		PreparedStatement ps = _c.prepareStatement("SELECT ACARS_ID FROM ACARS_PIREPS");
		ps.setFetchSize(1000);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			IDs.add(Integer.valueOf(rs.getInt(1)));
		
		rs.close();
		ps.close();
		
		// Set prepared statement
		ps = _c.prepareStatement("UPDATE ACARS_PIREPS SET TOTAL_FUEL=? WHERE (ACARS_ID=?)");
		
		// Load the ACARS data
		int totalFlights = 0;
		GetACARSData acdao = new GetACARSData(_c);
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
			Collection<RouteEntry> entries = acdao.getRouteEntries(id, info.getArchived());
			FuelUse use = FuelUse.validate(entries);
			
			// Update the data
			if (use.getTotalFuel() > 0) {
				ps.setInt(1, use.getTotalFuel());
				ps.setInt(2, id);
				ps.executeUpdate();
			}
			
			if ((totalFlights % 250) == 0)
				log.info(totalFlights + " flights updated");
		}
		
		// Commit
		ps.close();
		//_c.commit();
	}
}