package org.deltava;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import org.apache.log4j.*;
import org.jdom2.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class GateWriter extends TestCase {
	
	private static Logger log;
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common";
	
	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(GatePIREPLoader.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testGateXML() throws Exception {
		
		log.info("Loading Gates");
		Map<Airport, Collection<Gate>> allGates = new LinkedHashMap<Airport, Collection<Gate>>();
		GetGates gdao = new GetGates(_c);
		Collection<Gate> gates = gdao.getAll();
		for (Gate g: gates) {
			Airport a = SystemData.getAirport(g.getCode());
			if ((a == null) || (a.getRegion() == null)) continue;
			assertNotNull(a.getRegion());
			CollectionUtils.addMapCollection(allGates, a, g, ArrayList::new);
		}
		
		// Write the data files
		allGates.entrySet().parallelStream().forEach(this::writeGates);
	}
	
	private void writeGates(Map.Entry<Airport, Collection<Gate>> me) {
		final NumberFormat df = new DecimalFormat("#0.000000");
		
		Airport a = me.getKey();
		Document doc = new Document();
		Element re = new Element("gates");
		re.setAttribute("icao", a.getICAO());
		re.setAttribute("region", a.getRegion());
		doc.setRootElement(re);
		for (Gate g : me.getValue()) {
			Element ge = new Element("gate");
			ge.setAttribute("name", g.getName().startsWith("GATE ") ? g.getName().substring(5) : g.getName());
			ge.setAttribute("sim", g.getSimulator().toString());
			ge.setAttribute("type", g.getGateType().toString());
			ge.setAttribute("hdg", String.valueOf(g.getHeading()));
			ge.setAttribute("lat", df.format(g.getLatitude()));
			ge.setAttribute("lng", df.format(g.getLongitude()));
			ge.setAttribute("zone", g.getZone().name());
			g.getAirlines().forEach(al -> ge.addContent(XMLUtils.createElement("airline", al.getCode(), false)));
			re.addContent(ge);
		}
		
		// Write the document
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("C:\\Temp\\gates", String.format("gate_%s.xml", a.getICAO().toLowerCase()))))) {
			try (PrintWriter pw = new PrintWriter(os)) {
				pw.println(XMLUtils.format(doc, "utf-8"));	
			}
		} catch (IOException ie) {
			log.error("Error writing " + me.getKey() + " - " + ie.getMessage());
		}
	}
}