// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;

import org.deltava.util.*;

import junit.framework.TestCase;

public class XPlaneGateLoader extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common?useSSL=false";
	
	private static final Simulator SIM = Simulator.XP11;
	private static final String DATA_FILE = "apt11.dat";
	
	private Logger log;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(XPlaneGateLoader.class);
		
		// Connect to the database
		Class<?> c = Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		assertNotNull(c);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	public void testLoadXPGates() throws Exception {
		
		File f = new File("E:\\Temp", DATA_FILE);
		assertTrue(f.exists());
		
		// Load existing airport codes
		Collection<String> apCodes = new HashSet<String>();
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
			try (PreparedStatement ps = c.prepareStatement("SELECT CODE FROM common.NAVDATA WHERE (ITEMTYPE=?)")) {
				ps.setInt(1, Navaid.AIRPORT.ordinal());
				ps.setFetchSize(500);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						apCodes.add(rs.getString(1));
				}
			}
		}
		
		String apCode = null;
		Collection<Gate> gates = new ArrayList<Gate>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 262144)) {
			// Header
			String data = lr.readLine(); final List<String> dd = new ArrayList<String>();
			do {
				data = lr.readLine();
				if ((data == null) || (data.length() < 10)) continue;
				String type = data.substring(0, 4).trim(); dd.clear();
				if (type.indexOf(' ') > -1)
					type = type.substring(0, type.indexOf(' '));
				
				StringTokenizer tkns = new StringTokenizer(data.substring(4), " ");
				while (tkns.hasMoreTokens())
					dd.add(tkns.nextToken());
				
				if ("1".equals(type)) {
					String ap = dd.get(3).toUpperCase();
					apCode = apCodes.contains(ap) ? ap : null; 
					if (apCode != null) log.info("Processing " + apCode + " - line " + lr.getLineNumber());
					continue;
				} else if ("17".equals(type) || "16".equals(type)) {
					apCode = null;
					continue;
				}

				if ((apCode == null) || (!"1300".equals(type))) continue;
				if (!"gate".equalsIgnoreCase(dd.get(3))) continue;
				
				List<String> acTypes = StringUtils.split(dd.get(4), "|");
				if (!acTypes.contains("heavy") && !acTypes.contains("jets") && !acTypes.contains("turboprops") && !acTypes.contains("all")) continue;
				
				// Get gate position
				Gate g = new Gate(StringUtils.parse(dd.get(0), 0d), StringUtils.parse(dd.get(1), 0d));
				g.setCode(apCode);
				g.setHeading(Math.round(Float.parseFloat(dd.get(2))));
				
				// Get name
				StringBuilder buf = new StringBuilder();
				for (int x = 5; x < dd.size(); x++)
					buf.append(dd.get(x)).append(' ');
				
				g.setName(buf.toString().trim());
				if (g.getName().length() > 15)
					log.warn("Long name - " + g.getName());
				else
					gates.add(g);
			} while (data != null);
		}
		
		
		// Write data
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
			c.setAutoCommit(false);

			// Clear the table
			/* try (PreparedStatement ps = _c.prepareStatement("DELETE FROM common.GATES WHERE (SIMVERSION=?)")) { 
				ps.setInt(1, SIM.getCode());
				ps.executeUpdate();
			} */

			int rowsWritten = 0;
			try (PreparedStatement ps = c.prepareStatement("INSERT INTO common.GATES (ICAO, NAME, SIMVERSION, LATITUDE, LONGITUDE, HDG) VALUES (?, ?, ?, ?, ?, ?) AS G ON DUPLICATE KEY UPDATE LATITUDE=G.LATITUDE, LONGITUDE=G.LONGITUDE, HDG=G.HDG")) {
				for (Gate g : gates) {
					ps.setString(1, g.getCode());
					ps.setString(2, g.getName());
					ps.setInt(3, SIM.getCode());
					ps.setDouble(4, g.getLatitude());
					ps.setDouble(5, g.getLongitude());
					ps.setInt(6, g.getHeading());
					ps.addBatch(); rowsWritten++;
					if ((rowsWritten % 100) == 0) {
						ps.executeBatch();
						log.info("Wrote " + rowsWritten + " gates");
					}
				}
				
				if ((rowsWritten % 2500) != 0)
					ps.executeBatch();
				
				c.commit();
				log.info("Wrote " + rowsWritten + " gates");
			}
		}
	}
}