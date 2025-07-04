// Copyright 2009, 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.*;

import org.deltava.util.EnumUtils;
import org.deltava.util.StringUtils;
import org.jdom2.*;
import org.jdom2.filter.ElementFilter;

public class SimRunwayLoader extends SceneryLoaderTestCase {

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common?rewriteBatchedStatements=true&useSSL=false";
	private Connection _c;

	private static final Simulator SIM = Simulator.P3Dv4;
	private static final String XML_PATH = "E:\\temp\\rwy\\";

	private static final int WGS84_SRID = 4326;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log = LogManager.getLogger(SimRunwayLoader.class);

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	private static String formatLocation(double lat, double lng) {
		return String.format("POINT(%1$,.4f %2$,.4f)", Double.valueOf(lat), Double.valueOf(lng));
	}

	public void testLoadXML() throws Exception {

		File xp = new File(XML_PATH, SIM.name());
		assertTrue(xp.isDirectory());

		// Load ICAO codes
		Collection<String> codes = new HashSet<String>();
		try (PreparedStatement ps = _c.prepareStatement("SELECT DISTINCT CODE FROM common.NAVDATA WHERE (ITEMTYPE=?)")) {
			ps.setInt(1, Navaid.AIRPORT.ordinal());
			ps.setFetchSize(1000);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					codes.add(rs.getString(1));
			}
		}

		// Clear the table
		try (PreparedStatement ps = _c.prepareStatement("DELETE FROM common.RUNWAYS WHERE (SIMVERSION=?)")) {
			ps.setInt(1, SIM.getCode());
			ps.executeUpdate();
		}
		
		log.info("Loaded " + codes.size() + " Airports, purged " + SIM + " runways");
		
		// Load the widths from R5.csv
		File cf = new File(xp, "r5.csv");
		assertTrue(cf.exists() && cf.isFile());
		Map<String, Integer> widths = new HashMap<String, Integer>();
		Map<String, Double> magVar = new HashMap<String, Double>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader(cf))) {
			String data = lr.readLine();
			while (data != null) {
				List<String> tkns = StringUtils.split(data, ",");
				String apCode = tkns.get(0);
				String rwyNumber = tkns.get(1).substring(1, 3);
				switch (tkns.get(1).charAt(3)) {
					case '1':
						rwyNumber += "L";
						break;
						
					case '2':
						rwyNumber += "R";
						break;
						
					case '3':
						rwyNumber += "C";
						break;
						
					default:
						break;
				}
				
				if (codes.contains(apCode)) {
					double mV = Double.parseDouble(tkns.get(9)) * -1;
					widths.put(apCode + "$" + rwyNumber, Integer.valueOf(StringUtils.parse(tkns.get(8), -1)));
					magVar.putIfAbsent(apCode, Double.valueOf(mV));
					magVar.put(apCode + "$" + rwyNumber, Double.valueOf(mV));
				}
				
				data = lr.readLine();
			}
		}
		
		// Load the XML file
		File xf = new File(xp, "Runways.xml");
		assertTrue(xf.exists() && xf.isFile());

		Document doc = loadXML(new FileReader(xf));
		assertNotNull(doc);

		// Init the prepared statement
		int totalRwys = 0;
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO common.RUNWAYS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))")) {
			ps.setInt(3, SIM.getCode());

			// Get the airports
			Iterator<Element> ali = doc.getDescendants(new ElementFilter("ICAO"));
			while (ali.hasNext()) {
				Element ae = ali.next();
				String apCode = ae.getAttributeValue("id").toUpperCase();
				if (!codes.contains(apCode))
					continue;

				int hasData = 0;
				ps.setString(1, apCode);

				Iterator<Element> rli = ae.getDescendants(new ElementFilter("Runway"));
				while (rli.hasNext()) {
					Element re = rli.next();
					String rwyID = re.getAttributeValue("id");
					double lat = Double.parseDouble(re.getChildTextTrim("Lat"));
					double lng = Double.parseDouble(re.getChildTextTrim("Lon"));

					// Get the heading/length/width
					double hdg = Double.parseDouble(re.getChildTextTrim("Hdg"));
					float length = Float.parseFloat(re.getChildTextTrim("Len"));
					Integer w = widths.getOrDefault(apCode + "$" + rwyID, Integer.valueOf(-1));
					Double mV = magVar.getOrDefault(apCode + "$" + rwyID, magVar.get(apCode));
					if (w.intValue() < 0)
						log.warn("Cannot get runway width for " + rwyID + " at " + apCode);
					if (mV == null) {
						log.warn("Cannot get magnetic declination for " + rwyID + " at " + apCode);
						mV = Double.valueOf(0);
					}

					Runway rwy = new Runway(lat, lng);
					rwy.setCode(apCode);
					rwy.setName(rwyID);
					rwy.setHeading((int) Math.round(hdg - mV.doubleValue()));
					rwy.setLength(Math.round(length));
					rwy.setWidth(w.intValue());
					rwy.setMagVar(mV.doubleValue());
					rwy.setThresholdLength(Integer.parseInt(re.getChildTextTrim("ThresholdOffset")));
					rwy.setSurface(EnumUtils.parse(Surface.class, re.getChildTextTrim("Def").replace('-', '_'), Surface.UNKNOWN));
					if (rwy.getSurface() == Surface.UNKNOWN)
						log.warn("Unknown surface - " + re.getChildTextTrim("Def"));
					
					// Save the data
					if (rwy.getName().length() <= 4) {
						ps.setString(2, rwy.getName());
						ps.setDouble(4, rwy.getLatitude());
						ps.setDouble(5, rwy.getLongitude());
						ps.setInt(6, rwy.getHeading());
						ps.setInt(7, rwy.getLength());
						ps.setInt(8, rwy.getWidth());
						ps.setDouble(9, rwy.getMagVar());
						ps.setInt(10, rwy.getSurface().ordinal());
						ps.setInt(11, rwy.getThresholdLength());
						ps.setString(12, formatLocation(lat, lng));
						ps.setInt(13, WGS84_SRID);
						ps.addBatch();
						hasData++;
					} else
						log.warn("Skipping " + apCode + " runway " + rwy.getName());
				}

				// Save the entries
				if (hasData > 0) {
					ps.executeBatch();
					// _c.rollback();
					_c.commit();
					log.debug("Processing " + hasData + " runways for " + apCode);
					totalRwys += hasData; hasData = 0;
				}
			}
		}
		
		log.info("Loaded " + totalRwys + " runways into " + SIM);
	}
}