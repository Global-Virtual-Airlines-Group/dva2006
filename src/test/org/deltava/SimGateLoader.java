// Copyright 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.*;

import org.jdom2.*;
import org.jdom2.filter.ElementFilter;

public class SimGateLoader extends SceneryLoaderTestCase {

	private static final String JDBC_URL = "jdbc:mysql://polaris/common";
	private Connection _c;

	private static final String SCENERY_ROOT = "D:\\Program Files\\FSX\\Addon Scenery\\VHHX\\scenery";
	private static final String XML_PATH = "E:\\temp\\bgxml_vhhx-x";

	private static final String BGLXML = "data/bglxml/bglxml.exe";
	private static final Simulator SIM = Simulator.FSX;

	final class SceneryFilter implements FileFilter {
		private final String _prefix;

		SceneryFilter() {
			this("");
		}

		SceneryFilter(String prefix) {
			_prefix = prefix;
		}

		@Override
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && fn.startsWith(_prefix) && fn.endsWith(".bgl"));
		}
	}

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
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	public void testConvertBGLs() throws Exception {
		// assertFalse(true);

		// Check that we're running Windows and the file exists
		assertTrue(System.getProperty("os.name").contains("Windows"));
		File exe = new File(BGLXML);
		assertTrue(exe.exists() && exe.isFile());

		File rt = new File(SCENERY_ROOT);
		assertTrue(rt.isDirectory());

		Collection<File> bglFiles = getSingleFiles(rt, new SceneryFilter());
		assertNotNull(bglFiles);

		// Process the BGLs
		for (Iterator<File> i = bglFiles.iterator(); i.hasNext();) {
			File bgl = i.next();
			String fRoot = bgl.getName().substring(0, bgl.getName().lastIndexOf('.'));
			File xml = new File(XML_PATH, fRoot + ".xml");

			// Covert the BGL
			if (!xml.exists()) {
				log.info("Converting " + bgl.getCanonicalPath() + " to XML");
				ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath(), "-t", bgl.getPath(), xml.getPath());
				pb.redirectErrorStream(true);
				Process p = pb.start();
				int result = p.waitFor();
				if (result != 0) {
					try (InputStream is = new BufferedInputStream(p.getInputStream(), 512)) {
						try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
							while (br.ready())
								log.info(br.readLine());
						}
					}

					fail("Cannot convert to XML");
				}

				// Load the XML
				Document doc = null;
				if (xml.exists()) {
					try {
						filterAmpersands(xml);
						doc = loadXML(new FileReader(xml));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

				assertTrue(!xml.exists() || (doc != null));
			}
		}
	}

	public void testLoadXML() throws Exception {
		// assertTrue(false);

		File rt = new File(XML_PATH);
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

		// Clear the table
		/*
		 * try (PreparedStatement ps = _c.prepareStatement("DELETE FROM common.GATES WHERE (SIMVERSION=?)")) { ps.setInt(1, SIM.getCode());
		 * ps.executeUpdate(); }
		 */

		// Init the prepared statement
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO common.GATES (ICAO, NAME, SIMVERSION, LATITUDE, LONGITUDE, HDG) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(3, SIM.getCode());

			// Load the XML files
			File[] xmls = rt.listFiles(new XMLFilter());
			assertNotNull(xmls);
			for (int x = 0; x < xmls.length; x++) {
				log.info("Loading " + xmls[x]);
				Document doc = loadXML(new FileReader(xmls[x]));
				assertNotNull(doc);

				// Get the airports
				Iterator<Element> ali = doc.getDescendants(new ElementFilter("Airport"));
				while (ali.hasNext()) {
					Element ae = ali.next();
					String apCode = ae.getAttributeValue("ident").toUpperCase();
					if (!codes.contains(apCode))
						continue;

					log.info("Processing " + apCode + " from " + xmls[x]);
					ps.setString(1, apCode);

					Map<String, Gate> gates = new HashMap<String, Gate>();
					Iterator<Element> rli = ae.getDescendants(new ElementFilter("TaxiwayParking"));
					while (rli.hasNext()) {
						Element re = rli.next();
						double lat = Double.parseDouble(re.getAttributeValue("lat"));
						double lng = Double.parseDouble(re.getAttributeValue("lon"));
						float hdg = Float.parseFloat(re.getAttributeValue("heading"));

						// Get the gate code
						String gateCode = re.getAttributeValue("name", "GATE") + " " + re.getAttributeValue("number");
						gateCode = gateCode.replace('_', ' ');
						while (gateCode.indexOf("  ") > -1)
							gateCode = gateCode.replace("  ", " ");

						Gate g = new Gate(lat, lng);
						g.setCode(apCode);
						g.setName(gateCode);
						g.setHeading(Math.round(hdg));
						gates.put(gateCode, g);
					}

					// Save the entries
					if (!gates.isEmpty()) {
						for (Gate g : gates.values()) {
							ps.setString(2, g.getName());
							ps.setDouble(4, g.getLatitude());
							ps.setDouble(5, g.getLongitude());
							ps.setInt(6, g.getHeading());
							ps.addBatch();
						}

						ps.executeBatch();
						_c.commit();
					}
				}
			}
		}
	}
}