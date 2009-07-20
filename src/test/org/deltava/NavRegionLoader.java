// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.filter.ElementFilter;

public class NavRegionLoader extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL ="jdbc:mysql://polaris.sce.net/common";
	private Connection _c;
	
	private static final String SCENERY_ROOT = "E:\\Program Files\\Flight Simulator X\\Scenery";
	private static final String XML_PATH = "C:\\temp\\bgxml";
	
	private static final String BGLXML = "data/bglxml/bglxml.exe";
	
	private static final List<String> XML_ENAMES = Arrays.asList("Waypoint", "Vor", "Ndb");
	
	final class DirectoryFilter implements FileFilter {
		public boolean accept(File f) {
			return ((f != null) && f.isDirectory() && !f.getName().startsWith("."));
		}
	}
	
	final class SceneryFilter implements FileFilter {
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && (fn.startsWith("at") || fn.startsWith("nv")) && fn.endsWith(".bgl")
					&& !fn.equals("athens.bgl") && !fn.equals("atlanta.bgl"));
		}
	}
	
	final class XMLFilter implements FileFilter {
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && fn.endsWith(".xml"));
		}
	}
	
	private Collection<File> getFiles(File root) {
		Collection<File> files = new ArrayList<File>();
		File[] dirs = root.listFiles(new DirectoryFilter());
		assertNotNull(dirs);
		for (int x = 0; x < dirs.length; x++) {
			File f = new File(dirs[x], "scenery");
			if (f.isDirectory()) {
				File[] bgls = f.listFiles(new SceneryFilter());
				if (bgls != null)
					files.addAll(Arrays.asList(bgls));
			}
		}
		
		return files;
	}
	
	private Document loadXML(File f) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(f);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(ChartLoader.class);
		
		// Create the output directory
		File xmlP = new File(XML_PATH);
		if (!xmlP.exists())
			xmlP.mkdirs();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "14072");
		assertNotNull(_c);
		_c.setAutoCommit(false);
	}
	
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testConvertBGLs() throws Exception {
		
		// Check that we're running Windows and the file exists
		assertTrue(System.getProperty("os.name").contains("Windows"));
		File exe = new File(BGLXML);
		assertTrue(exe.exists() && exe.isFile());
		
		File rt = new File(SCENERY_ROOT);
		assertTrue(rt.isDirectory());
		
		Collection<File> bglFiles = getFiles(rt);
		assertNotNull(bglFiles);
		
		// Process the BGLs
		for (Iterator<File> i = bglFiles.iterator(); i.hasNext(); ) {
			File bgl = i.next();
			String fRoot = bgl.getName().substring(0, bgl.getName().lastIndexOf('.'));
			File xml = new File(XML_PATH, fRoot + ".xml");
			
			// Covert the BGL
			log.info("Converting " + bgl.getName() + " to XML");
			ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath(), "-t", bgl.getPath(), xml.getPath());
			Process p = pb.start();
			int result = p.waitFor();
			if (result != 0) {
				InputStream is = new BufferedInputStream(p.getInputStream(), 512);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				while (br.ready())
					log.info(br.readLine());
				
				is.close();
				fail("Cannot convert to XML");
			}
			
			// Load the XML
			Document doc = null;
			try {
				doc = loadXML(xml);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// Process the document
			assertNotNull(doc);
		}
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
			Document doc = loadXML(xmls[x]);
			assertNotNull(doc);
			
			// Get the waypoints
			for (Iterator<String> ei = XML_ENAMES.iterator(); ei.hasNext(); ) {
				String wpType = ei.next();
				boolean isWP = "Waypoint".equals(wpType);
				Iterator<?> eli = doc.getDescendants(new ElementFilter(wpType));
				while (eli.hasNext()) {
					Element e = (Element) eli.next();
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