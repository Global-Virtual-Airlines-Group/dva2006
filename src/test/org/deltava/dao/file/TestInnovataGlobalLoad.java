package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetFullSchedule;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

public class TestInnovataGlobalLoad extends TestCase {

	private static Logger log;

	private Connection _c;
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";

	private final Collection<String> _aCodes = new HashSet<String>();
	private Collection<Airline> _airlines;

	private static final List<String> CODES = List.of("AF", "DL", "JM", "KL", "AM", "NW");
	private static final List<String> CS_CODES = List.of("AF");

	private final DateTimeFormatter _df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestInnovataGlobalLoad.class);

		// Set SystemData properties
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);

		// Connect to the database
		Class.forName(SystemData.get("jdbc.driver"));
		DriverManager.setLoginTimeout(3);
		_c = DriverManager.getConnection(JDBC_URL, "luke", "14072");
		assertNotNull(_c);
		log.info("Connected to database");
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));

		// Load Airlines
		GetAirline adao = new GetAirline(_c);
		_airlines = adao.getActive().values();
		_airlines.forEach(a -> _aCodes.addAll(a.getCodes()));
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		log.info("Disconnected");
	}

	/*
	 * Rules for adding a flight: 1. Range contains today. 2. Airline code is AF or DL and the codeshare field is blank. 3. Airline code is
	 * a codeshare (ie. NOT AF or DL) and the codeshare info field contains AF or DL.
	 */
	public void testLoadCSV() throws IOException {

		// Airline counts
		LocalDate now = LocalDate.now();
		Map<String, AtomicInteger> aCount = new TreeMap<String, AtomicInteger>();
		Collection<String> neededCodes = new LinkedHashSet<String>();

		// Build the file name
		File f = new File("c:\\temp\\innovata.csv.gz");
		assertTrue(f.exists());
		try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(f), 16384)) {

			// Get output file
			File of = new File("c:\\temp\\innovata.csv");
			if (of.exists())
				return;

			// Create the output file
			int yearAdjust = now.getYear() - 2015;
			try (PrintWriter out = new PrintWriter(of)) {
				int rowCount = 0;
				try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(gzis), 40960)) {
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
						LocalDate sd = LocalDate.parse(entries.get(5), _df).plusYears(yearAdjust);
						LocalDate ed = LocalDate.parse(entries.get(6), _df).plusYears(yearAdjust);
						boolean isOK = !now.isBefore(sd) && !now.isAfter(ed);

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
				}
			}
		}

		// Log the counts
		log.info(StringUtils.listConcat(neededCodes, ", "));
		log.info(aCount);
	}

	public void testLoadDAO() throws Exception {

		File f = new File("c:\\temp\\innovata.csv.gz");
		assertTrue(f.exists());

		// Load Airports
		GetAirport apdao = new GetAirport(_c);
		Collection<Airport> airports = apdao.getAll().values();
		assertNotNull(airports);
		assertFalse(airports.isEmpty());

		try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(f), 16384)) {
			GetAircraft acdao = new GetAircraft(_c);
			GetFullSchedule dao = new GetFullSchedule(gzis);
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setMainlineCodes(CODES);
			dao.setCodeshareCodes(CS_CODES);

			// Load the legs
			Collection<RawScheduleEntry> entries = dao.process();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			log.info("Loaded " + entries.size() + " entries");
		}
	}
}