// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;

import junit.framework.TestCase;

public class XPlaneRunwayLoader extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://localhost/common?useSSL=false";
	private static final double M_TO_MI = 0.000621371;
	
	private static final Surface[] SFCS = new Surface[] { Surface.UNKNOWN, Surface.ASPHALT, Surface.CONCRETE, Surface.GRASS,
			Surface.DIRT, Surface.GRAVEL, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN,
			Surface.UNKNOWN, Surface.SAND, Surface.WATER, Surface.ICE, Surface.UNKNOWN };
	
	private Logger log;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(RunwayLoader.class);
		
		// Connect to the database
		Class<?> c = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadXPRunways() throws Exception {
		
		File f = new File("/Users/luke/apt.dat");
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
		Collection<Runway> rwys = new ArrayList<Runway>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f))) {
			// Header
			String data = lr.readLine(); final List<String> dd = new ArrayList<String>();
			do {
				data = lr.readLine();
				if ((data == null) || (data.length() < 10)) continue;
				String type = data.substring(0, 4).trim();  dd.clear();
				if (type.indexOf(' ') > -1)
					type = type.substring(0, type.indexOf(' '));
				
				StringTokenizer tkns = new StringTokenizer(data.substring(4), " ");
				while (tkns.hasMoreTokens())
					dd.add(tkns.nextToken());
				
				if ("1".equals(type)) {
					String ap = dd.get(3).toUpperCase();
					apCode = apCodes.contains(ap) ? ap : null; 
					if (apCode != null) log.info("Processing " + apCode);
					continue;
				} else if ("17".equals(type) || "16".equals(type)) {
					apCode = null;
					continue;
				}

				if ((apCode == null) || (!"100".equals(type))) continue;
				
				// Get surface type
				int sfcType = StringUtils.parse(dd.get(1), 0); Surface s = SFCS[sfcType];

				// Get runway position, heading, length
				GeoLocation gl1 = new GeoPosition(StringUtils.parse(dd.get(8), 0.0d), StringUtils.parse(dd.get(9), 0.0d));
				GeoLocation gl2 = new GeoPosition(StringUtils.parse(dd.get(17), 0.0d), StringUtils.parse(dd.get(18), 0.0d));
				int l = GeoUtils.distanceFeet(gl1, gl2); double hdg = GeoUtils.course(gl1, gl2);

				// Get displaced threshold lengths (if any)
				double dt1 = StringUtils.parse(dd.get(10), 0.0d);
				double dt2 = StringUtils.parse(dd.get(19), 0.0d);
				if (dt1 > 0)
					gl1 = GeoUtils.bearingPoint(gl1, dt1 * M_TO_MI, hdg);
				if (dt2 > 0)
					gl2 = GeoUtils.bearingPoint(gl2, dt2 * M_TO_MI, GeoUtils.normalize(180 + hdg));
				
				Runway r1 = new Runway(gl1.getLatitude(), gl1.getLongitude());
				r1.setName(dd.get(7)); r1.setCode(apCode); r1.setSurface(s);
				r1.setHeading((int) GeoUtils.normalize(hdg));
				r1.setLength((int)(l - dt1));
				rwys.add(r1);

				Runway r2 = new Runway(gl2.getLatitude(), gl2.getLongitude());
				r2.setName(dd.get(16)); r2.setCode(apCode); r2.setSurface(s);
				r2.setHeading((int) GeoUtils.normalize(hdg + 180));
				r2.setLength((int)(l - dt2));
				rwys.add(r2);
				
			} while (data != null);
		}
		
		// Write data
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "test")) {
			c.setAutoCommit(false);
			try (PreparedStatement ps = c.prepareStatement("DELETE FROM common.RUNWAYS WHERE (SIMVERSION=?)")) {
				ps.setInt(1, Simulator.XP10.getCode());
				ps.executeUpdate();
			}

			int rowsWritten = 0;
			try (PreparedStatement ps = c.prepareStatement("INSERT INTO common.RUNWAYS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				for (Runway r : rwys) {
					ps.setString(1, r.getCode());
					ps.setString(2, r.getName());
					ps.setInt(3, Simulator.XP10.getCode());
					ps.setDouble(4, r.getLatitude());
					ps.setDouble(5, r.getLongitude());
					ps.setInt(6, r.getHeading());
					ps.setInt(7, r.getLength());
					ps.setDouble(8, 0);
					ps.setInt(9, r.getSurface().ordinal());
					ps.addBatch(); rowsWritten++;
					if ((rowsWritten % 100) == 0) {
						ps.executeBatch();
						log.info("Wrote " + rowsWritten + " runways");
					}
				}
				
				if ((rowsWritten % 100) != 0)
					ps.executeBatch();
				
				c.commit();
				log.info("Wrote " + rowsWritten + " runways");
			}
		}
	}
}