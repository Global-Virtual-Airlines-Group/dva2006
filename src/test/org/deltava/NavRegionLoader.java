// Copyright 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.jdom2.*;
import org.jdom2.filter.ElementFilter;

public class NavRegionLoader extends BGLLoaderTestCase {
	
	private static final String JDBC_URL ="jdbc:mysql://polaris.sce.net/common";
	private Connection _c;
	
	private static final List<String> XML_ENAMES = Arrays.asList("Waypoint", "Vor", "Ndb");

	final class SceneryFilter implements FileFilter {
		@Override
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && (fn.startsWith("at") || fn.startsWith("nv")) && fn.endsWith(".bgl")
					&& !fn.equals("athens.bgl") && !fn.equals("atlanta.bgl"));
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log = Logger.getLogger(NavRegionLoader.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
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
		
		// Clear the table
		Statement s = _c.createStatement();
		s.execute("TRUNCATE common.NAVREG");
		s.close();
		
		// Init the prepared statement
		PreparedStatement ps = _c.prepareStatement("REPLACE INTO common.NAVREG VALUES (ROUND(?,1), ROUND(?,1), ?)");
		
		// Load the XML files
		File[] xmls = rt.listFiles(new XMLFilter());
		assertNotNull(xmls);
		for (int x = 0; x < xmls.length; x++) {
			log.info("Processing " + xmls[x].getName());
			Document doc = loadXML(new FileReader(xmls[x]));
			assertNotNull(doc);
			
			// Get the waypoints
			for (Iterator<String> ei = XML_ENAMES.iterator(); ei.hasNext(); ) {
				String wpType = ei.next();
				boolean isWP = "Waypoint".equals(wpType);
				Iterator<Element> eli = doc.getDescendants(new ElementFilter(wpType));
				while (eli.hasNext()) {
					Element e = eli.next();
					double lat = Double.parseDouble(e.getAttributeValue("lat"));
					double lng = Double.parseDouble(e.getAttributeValue("lon"));
					String region = e.getAttributeValue(isWP ? "waypointRegion" : "region");
					
					// Save the data
					ps.setDouble(1, lat);
					ps.setDouble(2, lng);
					ps.setString(3, region);
					ps.addBatch();
				}
			}
			
			// Save the entries
			ps.executeBatch();
			_c.commit();
		}
		
		// Clean up
		ps.close();
	}
}