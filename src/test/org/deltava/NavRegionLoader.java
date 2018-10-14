// Copyright 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.util.StringUtils;

public class NavRegionLoader extends TestCase {

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common?useSSL=false";
	private Connection _c;

	private Logger log;

	private static final int WGS84_SRID = 4326;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(NavRegionLoader.class);

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
	}

	private static String formatLocation(GeoLocation loc) {
		return String.format("POINT(%1$,.4f %2$,.4f)", Double.valueOf(loc.getLatitude()), Double.valueOf(loc.getLongitude()));
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	public void testLoadXML() throws Exception {
		File ft = new File("C:\\Temp\\earth_fix.dat");
		assertTrue(ft.isFile());
		File nt = new File("C:\\Temp\\earth_nav.dat");
		assertTrue(nt.isFile());

		// Clear the table
		try (Statement s = _c.createStatement()) {
			s.execute("TRUNCATE common.NAVREGIONS");
		}

		// Init the prepared statement
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO common.NAVREGIONS VALUES (?, ?, ?, ST_PointFromText(?,?), ?)")) {

			// Load the FIX file
			try (LineNumberReader lr = new LineNumberReader(new FileReader(ft), 65536)) {
				lr.readLine(); String data = lr.readLine();
				while (data != null) {
					data = lr.readLine();
					if ((data == null) || (data.length() < 44))
						continue;

					double lat = StringUtils.parse(data.substring(0, 14).trim(), -91.0);
					double lng = StringUtils.parse(data.substring(15, 29).trim(), -181.0);
					String code = data.substring(30, 35).trim().toUpperCase();
					String region = data.substring(41, 43).trim().toUpperCase();

					ps.setString(1, code);
					ps.setDouble(2, lat);
					ps.setDouble(3, lng);
					ps.setString(4, formatLocation(new GeoPosition(lat, lng)));
					ps.setInt(5, WGS84_SRID);
					ps.setString(6, region);
					ps.addBatch();
					if ((lr.getLineNumber() % 50) == 0) {
						log.info("Processing FIX line " + lr.getLineNumber());
						ps.executeBatch();
						_c.commit();
					}
				}
			}

			// Save the entries
			ps.executeBatch();
			_c.commit();
			
			// Load the navdata
			try (LineNumberReader lr = new LineNumberReader(new FileReader(nt), 65536)) {
				lr.readLine(); String data = lr.readLine();
				while (data != null) {
					data = lr.readLine();
					if ((data == null) || (data.length() < 81)) continue;
					
					int type = StringUtils.parse(data.substring(0, 2).trim(), 0);
					if ((type != 2) && (type !=3)) continue;
					
					double lat = StringUtils.parse(data.substring(3, 17).trim(), -91.0);
					double lng = StringUtils.parse(data.substring(18, 32).trim(), -181.0);
					String code = data.substring(68, 71).trim().toUpperCase();
					String region = data.substring(77, 80).trim().toUpperCase();
					
					ps.setString(1, code);
					ps.setDouble(2, lat);
					ps.setDouble(3, lng);
					ps.setString(4, formatLocation(new GeoPosition(lat, lng)));
					ps.setInt(5, WGS84_SRID);
					ps.setString(6, region);
					ps.addBatch();
					if ((lr.getLineNumber() % 50) == 0) {
						log.info("Processing NAV line " + lr.getLineNumber());
						ps.executeBatch();
						_c.commit();
					}
				}
			}
			
			// Save the entries
			ps.executeBatch();
			_c.commit();
		}
	}
}