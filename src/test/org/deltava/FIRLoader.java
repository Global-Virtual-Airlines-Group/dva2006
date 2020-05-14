package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.jdom2.*;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.FIR;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.StringUtils;

public class FIRLoader extends TestCase {

	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common";

	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(FIRLoader.class);

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadFIRs() throws Exception {

		File f = new File("c:\\temp\\firxs.xml");
		assertTrue(f.exists());

		// Load the document
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(f);
		assertNotNull(doc);

		// Get the root element
		Element re = doc.getRootElement();
		assertNotNull(re);
		assertEquals("kml", re.getName());

		Collection<String> IDs = new LinkedHashSet<String>();
		Collection<FIR> FIRs = new ArrayList<FIR>();
		Namespace ns = Namespace.getNamespace("http://earth.google.com/kml/2.0");

		// Loop through each placemark
		for (Iterator<Element> i = re.getDescendants(new ElementFilter("Placemark", ns)); i.hasNext();) {
			Element pe = i.next();

			FIR fir = new FIR(pe.getChildTextTrim("name", ns));
			fir.setName(pe.getChildTextTrim("description", ns));
			fir.setOceanic(IDs.contains(fir.getID()));
			IDs.add(fir.getID());
			log.info("Processing " + fir);

			// Load the coordinates
			Element lse = pe.getChild("LineString", ns);
			assertNotNull(lse);
			List<String> coords = StringUtils.split(lse.getChildTextNormalize("coordinates", pe.getNamespace()), " ");
			for (String coord : coords) {
				StringTokenizer tkns = new StringTokenizer(coord, ",");
				double lng = Double.parseDouble(tkns.nextToken());
				double lat = Double.parseDouble(tkns.nextToken());
				fir.addBorderPoint(new GeoPosition(lat, lng));
			}

			FIRs.add(fir);
		}

		// Init prepared statements
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO FIR (ID, OCEANIC, NAME) VALUES (?, ?, ?)")) {
			try (PreparedStatement ps2 = _c.prepareStatement("INSERT INTO FIRDATA (ID, OCEANIC, SEQ, LAT, LNG) VALUES (?, ?, ?, ?, ?)")) {

				// Write to the database
				for (FIR fir : FIRs) {
					ps.setString(1, fir.getID());
					ps.setBoolean(2, fir.isOceanic());
					ps.setString(3, fir.getName());

					// Add the border
					ps2.setString(1, fir.getID());
					ps2.setBoolean(2, fir.isOceanic());
					int seq = 0;
					for (GeoLocation loc : fir.getBorder()) {
						ps2.setInt(3, seq++);
						ps2.setDouble(4, loc.getLatitude());
						ps2.setDouble(5, loc.getLongitude());
						ps2.addBatch();
					}

					// write
					ps.executeUpdate();
					ps2.executeBatch();
					log.info("Wrote " + fir);
				}

				// Commit
				_c.commit();
			}
		}
	}

	public void testLoadFIRAliases() throws Exception {

		File f = new File("c:\\temp\\fir_alias.txt");
		assertTrue(f.exists());

		// Init prepared statement
		try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO FIRALIAS (ID, OCEANIC, ALIAS) VALUES (?, ?, ?)")) {
			try (LineNumberReader lr = new LineNumberReader(new FileReader(f))) {
				while (lr.ready()) {
					String data = lr.readLine();
					if ((data.length() < 5) || (data.startsWith(";")))
						continue;

					// Get the aliases
					List<String> parts = StringUtils.split(data, "/");
					if ((parts.size() < 3) || StringUtils.isEmpty(parts.get(2)))
						continue;

					// Get the code
					String code = parts.get(0);
					if ((parts.size() > 3) && !StringUtils.isEmpty(parts.get(3)))
						code = parts.get(3);
					if ("0000".equals(code))
						continue;

					// Save in the database
					String name = parts.get(1);
					boolean isOceanic = name.toLowerCase().contains("oceanic");
					log.info("Processing " + name + " (" + parts.get(2) + "->" + code + ")");
					ps.setString(1, code.toUpperCase());
					ps.setBoolean(2, isOceanic);
					ps.setString(3, parts.get(2));
					ps.executeUpdate();
				}
			}

			// Commit
			_c.commit();
		}
	}
}