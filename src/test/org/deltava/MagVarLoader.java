// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.jdom2.*;
import org.jdom2.filter.ElementFilter;

public class MagVarLoader extends BGLLoaderTestCase {

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common";
	private Connection _c;

	final class SceneryFilter implements FileFilter {
		@Override
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && (fn.startsWith("ap")));
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log = Logger.getLogger(NavRegionLoader.class);

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
		convertBGLs(new SceneryFilter());
	}

	public void testLoadXML() throws Exception {
		File rt = new File(XML_PATH);
		assertTrue(rt.isDirectory());

		// Init the prepared statement
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO common.MAGVAR (ICAO, MAGVAR) VALUES (?, ?)")) {

			// Load the XML files
			File[] xmls = rt.listFiles(new XMLFilter());
			assertNotNull(xmls);
			for (int x = 0; x < xmls.length; x++) {
				log.info("Processing " + xmls[x].getName());
				Document doc = null;
				try {
					try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xmls[x])))) {
						StringWriter xw = new StringWriter();
						try (PrintWriter pw = new PrintWriter(xw)) {
							while (br.ready()) {
								String data = br.readLine();
								data = data.replace('&', '_');
								pw.println(data);
							}
						}

						doc = loadXML(new StringReader(xw.toString()));
						assertNotNull(doc);
					}
				} catch (Exception e) {
					log.info("Cannot load " + xmls[x].getName());
					continue;
				}

				// Get the airports
				Iterator<Element> eli = doc.getDescendants(new ElementFilter("Airport"));
				while (eli.hasNext()) {
					Element e = eli.next();
					double mv = Double.parseDouble(e.getAttributeValue("magvar"));
					mv = Math.min(50, Math.max(-50, mv));
					String icao = e.getAttributeValue("ident");
					log.info("Processing Airport " + icao);

					// Save the data
					if (icao.length() == 4) {
						ps.setString(1, icao);
						ps.setDouble(2, mv);
						ps.addBatch();
					}
				}

				// Save the entries
				ps.executeBatch();
				_c.commit();
			}
		}
	}
}