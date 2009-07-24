// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.filter.*;
import org.jdom.input.*;

import org.deltava.beans.schedule.*;

import org.deltava.crypt.*;
import org.deltava.dao.*;
import org.deltava.util.*;

public class ChartLoader extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL ="jdbc:mysql://localhost/common";
	private static final String XML = "/home/luke/charts/charts.xml";
	private static final String PDF_ROOT = "/home/luke/charts/published_pdfs";
	
	private static final String[] TYPES = {"???", "IAP", "IAP", "STAR", "DP", "APD"};
	
	private Document _doc;
	private Connection _c;
	
	private final Map<String, Airport> airports = new LinkedHashMap<String, Airport>();
	private final Map<Airport, Collection<MD5Chart>> charts = new HashMap<Airport, Collection<MD5Chart>>();
	
	private final Map<String, String> pdfs = new HashMap<String, String>();
	
	class PDFFilter implements FileFilter {
		public boolean accept(File f) {
			return f.isFile() && f.getName().toUpperCase().endsWith("PDF");
		}
	}
	
	private class MD5Chart extends Chart {
		private String _md5;
		
		MD5Chart(String name, Airport ap) {
			super(name, ap);
		}
		
		public String getHash() {
			if (_md5 != null)
				return _md5;
			
			// Calculate the MD5
			MessageDigester md = new MessageDigester("MD5", 10240);
			byte[] hash = md.digest(_buffer);
			_md5 = MessageDigester.convert(hash);
			return _md5;
		}
		
		public void setHash(String md5) {
			_md5 = md5.toLowerCase();
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		File xml = new File(XML);
		assertTrue(xml.exists());
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(ChartLoader.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		airports.putAll(apdao.getAll());
		assertFalse(airports.isEmpty());
		
		// Populate charts map
		for (Iterator<Airport> i = airports.values().iterator(); i.hasNext(); ) {
			Airport a = i.next();
			charts.put(a, new ArrayList<MD5Chart>());
		}
		
		// Load existing charts
		Collection<MD5Chart> dbCharts = loadCharts();
		assertFalse(dbCharts.isEmpty());
		log.info("Loaded " + dbCharts.size() + " charts");
		for (Iterator<MD5Chart> i = dbCharts.iterator(); i.hasNext(); ) {
			MD5Chart c = i.next();
			Collection<MD5Chart> apCharts = charts.get(c.getAirport());
			apCharts.add(c);
		}
		
		// Load PDFs and make them case insensitive
		File[] pdfNames = new File(PDF_ROOT).listFiles(new PDFFilter());
		for (int x = 0; x < pdfNames.length; x++) {
			File f = pdfNames[x];
			pdfs.put(f.getName().toUpperCase(), f.getName());
		}
		
		// Load the XML
		SAXBuilder builder = new SAXBuilder();
		_doc = builder.build(xml);
		assertNotNull(_doc);
		
		// Pretty the XML
		File out = new File(System.getProperty("java.io.tmpdir"), "charts.xml");
		if (!out.exists()) {
			PrintWriter pw = new PrintWriter(new FileOutputStream(out));
			pw.println(XMLUtils.format(_doc));
			pw.flush();
			pw.close();
			log.info("Formatted XML data");
		}
	}

	protected void tearDown() throws Exception {
		_c.close();
		_doc = null;
		LogManager.shutdown();
		super.tearDown();
	}
	
	private Collection<MD5Chart> loadCharts() throws SQLException {
		Collection<MD5Chart> results = new ArrayList<MD5Chart>();
		PreparedStatement ps = _c.prepareStatement("SELECT ID, ICAO, TYPE, IMGFORMAT, NAME, SIZE, HASH "
				+ "FROM common.CHARTS ORDER BY ID");
		ps.setFetchSize(100);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			MD5Chart c = new MD5Chart(rs.getString(5), airports.get(rs.getString(2)));
			c.setID(rs.getInt(1));
			c.setType(rs.getInt(3));
			c.setImgType(rs.getInt(4));
			c.setSize(rs.getInt(6));
			c.setHash(rs.getString(7));
			results.add(c);
		}
		
		rs.close();
		ps.close();
		return results;
	}

	public void testUpdatedCharts() throws Exception {
		Element re = _doc.getRootElement();
		Collection<Element> chartEs = new ArrayList<Element>();
		Iterator<?> aI = re.getDescendants(new ElementFilter("airport_name"));
		while (aI.hasNext()) {
			Element e = (Element) aI.next();
			String id = e.getAttributeValue("icao_ident").toUpperCase();
			if (airports.containsKey(id))
				chartEs.add(e);
		}
		
		// Flush the documnet
		log.info("Keeping charts for " + chartEs.size() + " airports");
		_doc = null;
		
		// Init counters
		int added = 0;
		int updated = 0;
		int deleted = 0;
		
		// Loop through the charts
		SetChart cwdao = new SetChart(_c);
		for (Iterator<Element> i = chartEs.iterator(); i.hasNext(); ) {
			Element e = i.next();
			Airport a = airports.get(e.getAttributeValue("icao_ident").toUpperCase());
			Map<String, MD5Chart> apCharts = CollectionUtils.createMap(charts.get(a), "name");
			Collection<String> chartNames = new HashSet<String>();
			log.info("Processing " + a.getName());
			
			// Get the charts
			Collection<?> cEs = e.getChildren("record");
			for (Iterator<?> ci = cEs.iterator(); ci.hasNext(); ) {
				Element ce = (Element) ci.next();
				
				// Get the chart name
				String chartName = ce.getChildTextTrim("chart_name").replace(",", "");
				chartNames.add(chartName);
				
				// Check for deletions
				String opCode = ce.getChildTextTrim("useraction");
				if ("D".equals(opCode)) {
					log.info("Skipping deleted chart " + chartName);
					continue;
				}
				
				// Check if the chart already exists
				File f = new File(PDF_ROOT, pdfs.get(ce.getChildTextTrim("pdf_name").toUpperCase()));
				if (!f.exists())
					log.error(f.getPath() + " not found!");
				else if (apCharts.containsKey(chartName)) {
					MD5Chart oc = apCharts.get(chartName);
					
					// Create the new chart entry
					MD5Chart c = new MD5Chart(chartName.replace("  ", " "), a);
					c.setID(oc.getID());
					c.load(new FileInputStream(f));
					c.setImgType(Chart.PDF);
					c.setType(StringUtils.arrayIndexOf(TYPES, ce.getChildTextTrim("chart_code"), 0));
					if ((c.getType() == Chart.ILS) && (!c.getName().contains("ILS")))
							c.setType(Chart.APR);
					
					// Check if the hash changed
					if ((c.getType() > 0) && (!c.getHash().equals(oc.getHash()))) {
						apCharts.remove(chartName);
						apCharts.put(chartName, c);
						log.info("Updated " + c.getName());
						cwdao.write(c);
						updated++;
					}
				} else {
					MD5Chart c = new MD5Chart(chartName, a);
					c.load(new FileInputStream(f));
					c.setImgType(Chart.PDF);
					c.setType(StringUtils.arrayIndexOf(TYPES, ce.getChildTextTrim("chart_code"), 0));
					if ((c.getType() == Chart.ILS) && (!c.getName().contains("ILS")))
						c.setType(Chart.APR);
			
					if (c.getType() > 0) {
						apCharts.put(chartName, c);
						log.info("Added " + c.getName());
						cwdao.write(c);
						added++;
					}
				}
			}
			
			// Check what charts no longer appear for this airport
			Collection<String> oldNames = CollectionUtils.getDelta(apCharts.keySet(), chartNames);
			for (Iterator<String> ci = oldNames.iterator(); ci.hasNext(); ) {
				String chartName = ci.next();
				Chart oc = apCharts.get(chartName);
				if (oc != null) {
					log.info("Removing superceded chart " + oc.getName());
					cwdao.delete(oc.getID());
					deleted++;
				}
			}
		}

		// Write counts
		log.info("Added " + added +", updated " + updated + ", deleted " + deleted);
	}
}