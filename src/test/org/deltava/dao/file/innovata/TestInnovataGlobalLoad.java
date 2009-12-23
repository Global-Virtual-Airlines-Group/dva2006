package org.deltava.dao.file.innovata;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import java.util.concurrent.atomic.AtomicInteger;

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

	private final Collection<String> _aCodes = new HashSet<String>();
	private Collection<Airline> _airlines;

	private static final List<String> CODES = Arrays.asList("AF", "DL", "JM", "KL", "AM", "NW");
	private static final List<String> CS_CODES = Arrays.asList("AF");

	private static final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");

	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestInnovataGlobalLoad.class);
		
		// Check for local JDBC properties
		Properties dbp = new Properties();
		File f = new File("c:\\temp\\jdbc.properties");
		if (f.exists())
			dbp.load(new FileInputStream(f));
		else
			dbp.load(new FileInputStream("data/jdbc.properties"));
		
		// Set SystemData properties
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
		SystemData.add("jdbc.url", dbp.getProperty("url"));
		SystemData.add("jdbc.user", dbp.getProperty("user"));
		SystemData.add("jdbc.pwd", dbp.getProperty("password"));

		// Load JDBC properties
		Properties p = new Properties();
		p.putAll((Map) SystemData.getObject("jdbc.connectProperties"));
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
		
		// Load Database information
		log.info("Loading Cross-Application data");
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));
	}

	protected void tearDown() throws Exception {
		_c.close();
		log.info("Disconnected");
	}

	/*
	 * Rules for adding a flight: 1. Range contains today. 2. Airline code is AF or DL and the codeshare field is blank.
	 * 3. Airline code is a codeshare (ie. NOT AF or DL) and the codeshare info field contains AF or DL.
	 */
	public void testLoadCSV() throws IOException, ParseException {

		// Build the file name
		java.util.Date d = new java.util.Date();
		File f = new File("c:\\temp\\deltava - " + StringUtils.format(d, "MMM dd") + ".zip");
		assertTrue(f.exists());
		ZipFile zip = new ZipFile(f);
		assertTrue(zip.entries().hasMoreElements());
		ZipEntry ze = zip.entries().nextElement();
		assertNotNull(ze);

		// Get output file
		File of = new File("c:\\temp\\" + StringUtils.format(d, "MMMyy") + ".csv");
		if (of.exists())
			return;
		
		// Create the output file
		PrintWriter out = new PrintWriter(of);
		
		// Get the effective date
		long now = System.currentTimeMillis();
		//long now = StringUtils.parseDate("02/20/2010", "MM/dd/yyyy").getTime();
		
		// Airline counts
		Map<String, AtomicInteger> aCount = new TreeMap<String, AtomicInteger>();

		// Load the File
		int rowCount = 0;
		Collection<String> neededCodes = new LinkedHashSet<String>();
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
			if (isOK && !isMainLine && !CODES.contains(entries.get(3))) {
				String csInfo = entries.get(50);
				for (Iterator<String> i = CS_CODES.iterator(); i.hasNext() && !isCodeShare;) {
					String mlCode = i.next();
					isCodeShare |= (csInfo.indexOf(mlCode) != -1);
				}
			}

			// Add the entry
			if (isOK && (isMainLine || isCodeShare) && !isCS) {
				neededCodes.add(code);
				out.println(data);
				rowCount++;
				
				// Add the count
				AtomicInteger cnt = aCount.get(code);
				if (cnt == null)
					aCount.put(code, new AtomicInteger(1));
				else
					cnt.incrementAndGet();
				
				// Log data
				if ((rowCount % 100) == 0)
					log.info(rowCount + " entries added");
			}

			if ((lr.getLineNumber() % 100000) == 0)
				log.info(lr.getLineNumber() + " rows read");
		}

		// Close the streams
		lr.close();
		out.close();
		
		// Log the counts
		log.info(StringUtils.listConcat(neededCodes, ", "));
		log.info(aCount);
	}
	
	public void testParseCSV() throws Exception {

		// Build the file name
		java.util.Date d = new java.util.Date();
		File f = new File("c:\\temp\\deltava - " + StringUtils.format(d, "MMM dd") + ".zip");
		assertTrue(f.exists());
		ZipFile zip = new ZipFile(f);
		assertTrue(zip.entries().hasMoreElements());
		ZipEntry ze = zip.entries().nextElement();
		assertNotNull(ze);

		// Get the DAO
		GetAircraft acdao = new GetAircraft(_c);
		GetFullSchedule dao = new GetFullSchedule(zip.getInputStream(ze));
		dao.setEffectiveDate(new java.util.Date());
		dao.setAircraft(acdao.getAircraftTypes());
		dao.setMainlineCodes(CODES);
		dao.setCodeshareCodes(CS_CODES);
		
		// Load the legs
		Collection<CSVTokens> tkns = dao.load();
		
		// Get output file
		File of = new File("c:\\temp\\" + StringUtils.format(d, "MMMyy") + "2.csv");
		PrintWriter out = new PrintWriter(of);
		for (Iterator<CSVTokens> i = tkns.iterator(); i.hasNext(); ) {
			CSVTokens tkn = i.next();
			out.println(tkn.toString());
		}
		
		out.close();
	}

	public void testLoadDAO() throws Exception {

		File f = new File("c:/temp/" + StringUtils.format(new java.util.Date(), "MMMyy") + ".csv");
		assertTrue(f.exists());

		// Load Airports
		GetAirport apdao = new GetAirport(_c);
		Collection<Airport> airports = apdao.getAll().values();
		assertNotNull(airports);
		assertFalse(airports.isEmpty());

		// Get the DAO
		GetAircraft acdao = new GetAircraft(_c);
		GetFullSchedule dao = new GetFullSchedule(new FileInputStream(f));
		dao.setEffectiveDate(new java.util.Date());
		dao.setAircraft(acdao.getAircraftTypes());
		dao.setMainlineCodes(CODES);
		dao.setCodeshareCodes(CS_CODES);

		// Load the legs
		dao.load();
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		log.info("Loaded " + entries.size() + " entries");
		
		// Save the legs
		SetSchedule wdao = new SetSchedule(_c);
		wdao.purge(false);
		for (Iterator<ScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
			ScheduleEntry se = i.next();
			se.setCanPurge(true);
			wdao.write(se, true);
		}
	}
}