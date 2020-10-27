package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.*;

import org.deltava.util.*;

public class SimGateLoader extends SceneryLoaderTestCase {

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common";
	private Connection _c;

	private static final String XML_PATH = "E:\\temp\\rwy";

	private static final Simulator SIM = Simulator.FS2020;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log = Logger.getLogger(SimGateLoader.class);

		// Create the output directory
		File xmlP = new File(XML_PATH);
		if (!xmlP.exists())
			xmlP.mkdirs();

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

	public void testLoadXML() throws Exception {
		// assertTrue(false);

		File rt = new File(XML_PATH, SIM.name());
		assertTrue(rt.isDirectory());

		// Load ICAO codes
		Collection<String> codes = new HashSet<String>();
		try (PreparedStatement ps = _c.prepareStatement("SELECT DISTINCT CODE FROM common.NAVDATA WHERE (ITEMTYPE=?)")) {
			ps.setInt(1, Navaid.RUNWAY.ordinal());
			ps.setFetchSize(1000);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					codes.add(rs.getString(1));
			}
		}
		
		codes.clear();
		codes.add("KATL");

		// Clear the table
		/* try (PreparedStatement ps = _c.prepareStatement("DELETE FROM common.GATES WHERE (SIMVERSION=?)")) { 
			ps.setInt(1, SIM.getCode());
			ps.executeUpdate();
		} */
		
		// Open the file
		File f = new File(rt, "g5.csv");
		assertTrue(f.isFile());

		// Init the prepared statement
		try (PreparedStatement ps = _c.prepareStatement("INSERT INTO common.GATES (ICAO, NAME, SIMVERSION, LATITUDE, LONGITUDE, HDG) VALUES (?, ?, ?, ?, ?, ?) AS G ON DUPLICATE KEY UPDATE "
			+ "LATITUDE=G.LATITUDE, LONGITUDE=G.LONGITUDE, HDG=G.HDG")) {
			ps.setInt(3, SIM.getCode());

			// Load the CSV
			String lastAP = null; boolean hasData = false;
			try (InputStream is = new FileInputStream(f); LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 262144)) {
				String data = lr.readLine();
				while (data != null) {
					CSVTokens tkns = StringUtils.parseCSV(data);
					String apCode = tkns.get(0).toUpperCase();
					if (!codes.contains(apCode)) {
						data = lr.readLine();
						continue;
					}

					if (!apCode.equals(lastAP)) {
						if (hasData) ps.executeBatch();
						log.info("Processing " + apCode + " at Line " + lr.getLineNumber());
						ps.setString(1, apCode);
						lastAP = apCode;
						hasData = false;
					}
					
					String gateName = tkns.get(1);
					if ("Park".equalsIgnoreCase(gateName))
						gateName = "PARKING";
					else if (gateName.startsWith("Pk"))
						gateName = gateName.substring(2) + " PARKING";
					else
						gateName = "GATE " + gateName;
					
					try {
						String gateNumber = tkns.get(2);
						Gate g = new Gate(Double.parseDouble(tkns.get(3)), Double.parseDouble(tkns.get(4)));
						g.setCode(apCode);
						g.setSimulator(SIM);
						g.setName(gateName + " " + gateNumber);
						g.setHeading(Math.round(Float.parseFloat(tkns.get(6))));
						
						// Save the entry
						ps.setString(2, g.getName());
						ps.setDouble(4, g.getLatitude());
						ps.setDouble(5, g.getLongitude());
						ps.setInt(6, g.getHeading());
						ps.addBatch();
						hasData = true;
					} catch (Exception e) {
						log.warn(e.getMessage() + " at Line " + lr.getLineNumber());
					}
						
					data = lr.readLine();
				}
			}
			
			if (hasData) ps.executeBatch();
		}
		
		_c.commit();
	}
}