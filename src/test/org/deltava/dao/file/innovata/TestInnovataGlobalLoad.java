package org.deltava.dao.file.innovata;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.innovata.GetFullSchedule;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class TestInnovataGlobalLoad extends TestCase {

	private static Logger log;

	private Connection _c;

	private Collection<String> _aCodes = new HashSet<String>();
	private Collection<Airline> _airlines;

	private static final List<String> CODES = Arrays.asList(new String[] { "AF", "DL", "JM", "KL", "AM" }); // BD

	private static final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");

	protected void setUp() throws Exception {
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestInnovataGlobalLoad.class);
		
		// Load JDBC properties
		Properties dbp = new Properties();
		dbp.load(new FileInputStream("data/jdbc.properties"));
		
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
		SystemData.add("jdbc.url", dbp.getProperty("url"));
		SystemData.add("jdbc.user", dbp.getProperty("user"));
		SystemData.add("jdbc.pwd", dbp.getProperty("password"));

		// Load JDBC properties
		Properties p = new Properties();
		p.putAll((Map<? extends Object, ? extends Object>) SystemData.getObject("jdbc.connectProperties"));
		p.setProperty("user", SystemData.get("jdbc.user"));
		p.setProperty("password", SystemData.get("jdbc.pwd"));

		// Connect to the database
		Class.forName(SystemData.get("jdbc.driver"));
		_c = DriverManager.getConnection(SystemData.get("jdbc.url"), p);
		assertNotNull(_c);
		log.info("Connected to database");
		
		// Load Time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();

		// Load Airlines
		GetAirline adao = new GetAirline(_c);
		_airlines = adao.getActive().values();
		for (Iterator<Airline> i = _airlines.iterator(); i.hasNext();) {
			Airline a = i.next();
			_aCodes.addAll(a.getCodes());
		}
	}

	protected void tearDown() throws Exception {
		_c.close();
		log.info("Disconnected");
	}

	/*
	 * Rules for adding a flight: 1. Range contains today. 2. Airline code is AF or DL and the codeshare field is blank.
	 * 3. Airline code is a codeshare (ie. NOT AF or DL) and the codeshare info field contains AF or DL.
	 */
	public void testLoadCSV() throws IOException, SQLException, ParseException {

		File f = new File("c:\\temp\\deltava.zip");
		assertTrue(f.exists());
		ZipFile zip = new ZipFile(f);
		assertTrue(zip.entries().hasMoreElements());
		ZipEntry ze = zip.entries().nextElement();
		assertNotNull(ze);

		// Write output file
		PrintWriter out = new PrintWriter("c:\\temp\\Aug01.csv");

		// Load the File
		int rowCount = 0;
		Collection<String> neededCodes = new LinkedHashSet<String>();
		long now = System.currentTimeMillis();
		LineNumberReader lr = new LineNumberReader(new InputStreamReader(zip.getInputStream(ze)), 40960);
		lr.readLine();
		while (lr.ready()) {
			String data = lr.readLine();
			StringTokenizer tkns = new StringTokenizer(data, ",");
			List<String> entries = new ArrayList<String>(55);
			while (tkns.hasMoreTokens()) {
				String tkn = tkns.nextToken();
				if (tkn.charAt(0) == '\"')
					tkn = tkn.substring(1, tkn.length() - 1);

				entries.add(tkn);
			}

			// Check the date
			long sd = _df.parse(entries.get(5)).getTime();
			long ed = _df.parse(entries.get(6)).getTime();
			boolean isOK = (now >= sd) && (now <= ed);

			// Check codeshare operation
			boolean isCS = "1".equals(entries.get(48));

			// Check the airline
			String code = entries.get(0).toUpperCase();
			boolean isMainLine = CODES.contains(code);
			boolean isCodeShare = false;
			if (isOK && _aCodes.contains(code) && !isMainLine && !CODES.contains(entries.get(3))) {
				String csInfo = entries.get(50);
				for (Iterator<String> i = CODES.iterator(); i.hasNext() && !isCodeShare;) {
					String mlCode = i.next();
					isCodeShare |= (csInfo.indexOf(mlCode) != -1);
				}
			}

			// Add the entry
			if (isOK && (isMainLine || isCodeShare) && !isCS) {
				neededCodes.add(code);
				out.println(data);
				rowCount++;
				// Log data
				if ((rowCount % 100) == 0)
					log.info(rowCount + " entries added");
			}

			if ((lr.getLineNumber() % 100000) == 0)
				log.info(lr.getLineNumber() + " rows read");
		}

		// Close the streams
		log.info(StringUtils.listConcat(neededCodes, ", "));
		lr.close();
		out.close();
	}

	public void testLoadDAO() throws Exception {

		File f = new File("c:/temp/Aug01.csv");
		assertTrue(f.exists());

		// Load Airports
		GetAirport apdao = new GetAirport(_c);
		Collection<Airport> airports = apdao.getAll().values();
		assertNotNull(airports);
		assertFalse(airports.isEmpty());

		// Get the DAO
		GetFullSchedule dao = new GetFullSchedule(new FileInputStream(f));
		dao.setEffectiveDate(new java.util.Date());
		dao.setPrimaryCodes(Arrays.asList(new String[] { "DL", "AF" }));
		dao.setAirlines(_airlines);
		dao.setAirports(airports);

		// Load the legs
		dao.load();
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		log.info("Loaded " + entries.size() + " entries");
	}
}