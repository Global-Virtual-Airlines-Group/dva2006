// Copyright 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;

import junit.framework.TestCase;

public class XPlaneRunwayLoader extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common?useSSL=false&allowPublicKeyRetrieval=true";
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";
	
	private static final double FT_PER_M = 3.2808399;
	
	private static final Surface[] SFCS = { Surface.UNKNOWN, Surface.ASPHALT, Surface.CONCRETE, Surface.GRASS, Surface.DIRT, Surface.GRAVEL, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN, Surface.UNKNOWN,
			Surface.UNKNOWN, Surface.SAND, Surface.WATER, Surface.ICE, Surface.CONCRETE };
	
	private static final int WGS84_SRID = 4326;
	
	private static final Simulator SIM = Simulator.XP12;
	private static final String DATA_FILE = "apt12.dat";
	
	private Logger log;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(XPlaneRunwayLoader.class);
		
		// Connect to the database
		Class<?> c = Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		assertNotNull(c);
	}

	private static String formatLocation(GeoLocation loc) {
		return String.format("POINT(%1$,.4f %2$,.4f)", Double.valueOf(loc.getLatitude()), Double.valueOf(loc.getLongitude()));
	}

	public void testLoadXPRunways() throws Exception {
		
		File f = new File("E:\\Temp", DATA_FILE);
		assertTrue(f.exists());
		
		// Load existing airport codes
		Collection<String> apCodes = new HashSet<String>();
		try (Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD)) {
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
		try (LineNumberReader lr = new LineNumberReader(new FileReader(f), 65536)) {
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
					if (apCode != null) log.info("Processing " + apCode + " - line " + lr.getLineNumber());
					continue;
				} else if ("17".equals(type) || "16".equals(type)) {
					apCode = null;
					continue;
				}

				if ((apCode == null) || (!"100".equals(type))) continue;
				
				// Get surface type
				int sfcType = StringUtils.parse(dd.get(1), 0); Surface s = Surface.UNKNOWN; 
				if ((sfcType >= 20) && (sfcType <= 38))
					s = Surface.ASPHALT;
				else if ((sfcType >= 50) && (sfcType <= 57))
					s  = Surface.CONCRETE;
				else if ((sfcType > 0) && (sfcType < 16))
					s = SFCS[sfcType];
				
				if (s == Surface.UNKNOWN)
					log.warn("Unknown Runway Surface - {}", dd.get(1));

				// Get runway position, heading, length
				GeoLocation gl1 = new GeoPosition(StringUtils.parse(dd.get(8), 0.0d), StringUtils.parse(dd.get(9), 0.0d));
				GeoLocation gl2 = new GeoPosition(StringUtils.parse(dd.get(17), 0.0d), StringUtils.parse(dd.get(18), 0.0d));
				int l = gl1.distanceFeet(gl2); double hdg = GeoUtils.course(gl1, gl2);
				
				// Get width
				double width = StringUtils.parse(dd.get(0), 45.0);

				// Get displaced threshold lengths (if any)
				double dt1 = StringUtils.parse(dd.get(10), 0.0d); // meters
				double dt2 = StringUtils.parse(dd.get(19), 0.0d);
				
				String rwyName = dd.get(7); if (rwyName.length() == 2) rwyName = "0" + rwyName;
				Runway r1 = new Runway(gl1.getLatitude(), gl1.getLongitude());
				r1.setName(rwyName); r1.setCode(apCode); r1.setSurface(s);
				r1.setHeading((int) GeoUtils.normalize(hdg));
				r1.setLength(l);
				r1.setWidth((int) Math.round(width * FT_PER_M));
				r1.setThresholdLength((int) Math.round(dt1 * FT_PER_M));
				rwys.add(r1);

				Runway r2 = new Runway(gl2.getLatitude(), gl2.getLongitude());
				r2.setName(dd.get(16)); r2.setCode(apCode); r2.setSurface(s);
				r2.setHeading((int) GeoUtils.normalize(hdg + 180));
				r2.setLength((int)(l - dt2));
				r2.setWidth(r1.getWidth());
				r2.setThresholdLength((int) Math.round(dt2 * FT_PER_M));
				rwys.add(r2);
				
			} while (data != null);
		}
		
		// Write data
		try (Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD)) {
			c.setAutoCommit(false);
			try (PreparedStatement ps = c.prepareStatement("DELETE FROM common.RUNWAYS WHERE (SIMVERSION=?)")) {
				ps.setInt(1, SIM.getCode());
				ps.executeUpdate();
			}

			int rowsWritten = 0;
			try (PreparedStatement ps = c.prepareStatement("REPLACE INTO common.RUNWAYS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))")) {
				for (Runway r : rwys) {
					ps.setString(1, r.getCode());
					ps.setString(2, r.getName());
					ps.setInt(3, SIM.getCode());
					ps.setDouble(4, r.getLatitude());
					ps.setDouble(5, r.getLongitude());
					ps.setInt(6, r.getHeading());
					ps.setInt(7, r.getLength());
					ps.setInt(8, r.getWidth());
					ps.setDouble(9, 0);
					ps.setInt(10, r.getSurface().ordinal());
					ps.setInt(11, r.getThresholdLength());
					ps.setString(12, formatLocation(r));
					ps.setInt(13, WGS84_SRID);
					ps.addBatch(); rowsWritten++;
					if ((rowsWritten % 100) == 0) {
						ps.executeBatch();
						log.info("Wrote {} runways", Integer.valueOf(rowsWritten));
					}
				}
				
				if ((rowsWritten % 100) != 0)
					ps.executeBatch();
				
				c.commit();
				log.info("Wrote {} runways", Integer.valueOf(rowsWritten));
			}
		}
	}
}