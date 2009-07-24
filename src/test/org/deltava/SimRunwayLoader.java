// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.*;
import org.deltava.util.StringUtils;

import org.jdom.*;
import org.jdom.filter.ElementFilter;

public class SimRunwayLoader extends SceneryLoaderTestCase {
	
	private static final String JDBC_URL ="jdbc:mysql://polaris.sce.net/common";
	private Connection _c;

	private static final String SCENERY_ROOT = "E:\\Program Files\\Flight Simulator X\\Scenery";
	private static final String XML_PATH = "C:\\temp\\bgxml";
	
	private static final String BGLXML = "data/bglxml/bglxml.exe";
	private static final int SIM_VERSION = 2006;
	
	private static final String[] NAMES = {"NORTH", "SOUTH", "EAST", "WEST", "NORTHWEST", "SOUTHEAST", "NORTHEAST", "SOUTHWEST"};
	private static final String[] CODES = {"N", "S", "E", "W", "NW", "SE", "NE", "SW"};
	
	final class SceneryFilter implements FileFilter {
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && fn.startsWith("ap") && fn.endsWith(".bgl"));
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		log = Logger.getLogger(SimRunwayLoader.class);
		
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
		super.tearDown();
	}

	public void testConvertBGLs() throws Exception {
		
		// Check that we're running Windows and the file exists
		assertTrue(System.getProperty("os.name").contains("Windows"));
		File exe = new File(BGLXML);
		assertTrue(exe.exists() && exe.isFile());

		File rt = new File(SCENERY_ROOT);
		assertTrue(rt.isDirectory());
		
		Collection<File> bglFiles = getFiles(rt, new SceneryFilter());
		assertNotNull(bglFiles);
		
		// Process the BGLs
		for (Iterator<File> i = bglFiles.iterator(); i.hasNext(); ) {
			File bgl = i.next();
			String fRoot = bgl.getName().substring(0, bgl.getName().lastIndexOf('.'));
			File xml = new File(XML_PATH, fRoot + ".xml");
			
			// Covert the BGL
			if (!xml.exists()) {
				log.info("Converting " + bgl.getCanonicalPath() + " to XML");
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
					filterAmpersands(xml);
					doc = loadXML(xml);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			
				//Process the document
				assertNotNull(doc);
			}
		}
	}
	
	public void testLoadXML() throws Exception {
		//File rt = new File("C:\\temp");
		File rt = new File(XML_PATH);
		assertTrue(rt.isDirectory());
		
		// Load ICAO codes
		Collection<String> codes = new HashSet<String>();
		PreparedStatement ps = _c.prepareStatement("SELECT DISTINCT CODE FROM common.NAVDATA WHERE (ITEMTYPE=?)");
		ps.setInt(1, NavigationDataBean.RUNWAY);
		ps.setFetchSize(1000);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			codes.add(rs.getString(1));
		
		rs.close();
		ps.close();
		
		// Clear the table
		ps = _c.prepareStatement("DELETE FROM common.RUNWAYS WHERE (SIMVERSION=?)");
		ps.setInt(1, SIM_VERSION);
		ps.executeUpdate();
		ps.close();
		
		// Init the prepared statement
		ps = _c.prepareStatement("REPLACE INTO common.RUNWAYS (ICAO, NAME, SIMVERSION, LATITUDE, LONGITUDE, HDG, LENGTH) VALUES (?, ?, ?, ?, ?, ?, ?)");
		ps.setInt(3, SIM_VERSION);

		// Load the XML files
		File[] xmls = rt.listFiles(new XMLFilter());
		assertNotNull(xmls);
		for (int x = 0; x < xmls.length; x++) {
			Document doc = loadXML(xmls[x]);
			assertNotNull(doc);
			
			// Get the airports
			Iterator<?> ali = doc.getDescendants(new ElementFilter("Airport"));
			while (ali.hasNext()) {
				Element ae = (Element) ali.next();
				String apCode = ae.getAttributeValue("ident").toUpperCase();
				if (!codes.contains(apCode))
					continue;

				log.info("Processing " + apCode + " from " + xmls[x]);
				
				float magVar = Float.parseFloat(ae.getAttributeValue("magvar", "0.0"));
				Map<String, Runway> runways = new HashMap<String, Runway>();
				Iterator<?> rli = ae.getDescendants(new ElementFilter("Runway"));
				while (rli.hasNext()) {
					Element re = (Element) rli.next();
					double lat = Double.parseDouble(re.getAttributeValue("lat"));
					double lng = Double.parseDouble(re.getAttributeValue("lon"));
					
					// Get the heading
					float hdg = Float.parseFloat(re.getAttributeValue("heading"));
					hdg += magVar;
					
					// Get the runway length
					String rawLength = re.getAttributeValue("length");
					if (Character.isLetter(rawLength.charAt(rawLength.length() -1)))
						rawLength = rawLength.substring(0, rawLength.length() -2);	
						
					float length = Float.parseFloat(rawLength);
					length *= 3.2808399;
					
					// Get the runway code
					String number = re.getAttributeValue("number");
					String code = re.getAttributeValue("primaryDesignator");
					if (code != null) {
						code = code.substring(0, 1).toUpperCase();
						if (!"W".equals(code) && !"N".equals(code))
							number += code;
					} else if (Character.isLetter(number.charAt(0))) {
						int ofs = StringUtils.arrayIndexOf(NAMES, number);
						if (ofs > -1)
							number = CODES[ofs];
					}
					
					Runway rwy = new Runway(lat, lng);
					rwy.setCode(apCode);
					rwy.setName(number);
					rwy.setHeading(Math.round(hdg));
					rwy.setLength(Math.round(length));
					runways.put(number, rwy);
				}
				
				// Load the runway starts
				ps.setString(1, apCode);
				boolean hasData = false;
				Iterator<?> sli = ae.getDescendants(new ElementFilter("Start"));
				while (sli.hasNext()) {
					Element se = (Element) sli.next();
					String type = se.getAttributeValue("type");
					if (!"RUNWAY".equalsIgnoreCase(type))
						continue;
					
					String number = se.getAttributeValue("number");
					String code = se.getAttributeValue("designator");
					if (code != null) {
						code = code.substring(0, 1).toUpperCase();
						if (!"W".equals(code) && !"N".equals(code))
							number += code;
					}
					
					// Get the proper code
					if (Character.isLetter(number.charAt(0))) {
						int ofs = StringUtils.arrayIndexOf(NAMES, number);
						if (ofs > -1)
							number = CODES[ofs];
					}
					
					// Get the runway data
					double lat = Double.parseDouble(se.getAttributeValue("lat"));
					double lng = Double.parseDouble(se.getAttributeValue("lon"));
					float hdg = Float.parseFloat(se.getAttributeValue("heading"));
					hdg += magVar;
					if (hdg < 0)
						hdg += 360;
					
					// Get the length
					Runway r = runways.get(number);
					if (r == null) {
						String rawNumber = se.getAttributeValue("number");
						int newNumber = StringUtils.parse(rawNumber, -1);
						if (newNumber > 0) {
							newNumber += ((newNumber < 18) ? 18 : -18);
							String newCode = StringUtils.format(newNumber, "00");
							if (code.equals("L"))
								newCode += "R";
							if (code.equals("R"))
								newCode += "L";
							
							r = runways.get(newCode);
							if (r == null)
								log.warn("Cannot find runway " + newCode);
						} else {
							int ofs = StringUtils.arrayIndexOf(NAMES, rawNumber);
							String newCode = rawNumber;
							if (ofs > -1) {
								if ((ofs % 2) == 0)
									newCode = CODES[ofs + 1];
								else
									newCode = CODES[ofs - 1];
							}
							
							r = runways.get(newCode);
							if (r == null)
								log.warn("Cannot find runway " + newCode);
						}
					}

					// Save the data
					if ((number.length() <= 4) && (r != null)) {
						ps.setString(2, number);
						ps.setDouble(4, lat);
						ps.setDouble(5, lng);
						ps.setInt(6, Math.round(hdg));
						ps.setInt(7, r.getLength());
						ps.addBatch();
						hasData = true;
					} else
						log.warn("Skipping " + apCode + " runway " + number);
				}
				
				// Save the entries
				if (hasData) {
					ps.executeBatch();
					_c.commit();
				}
			}
		}
		
		// Clean up
		ps.close();
	}
}