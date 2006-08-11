package org.deltava.dao.file.innovata;

import java.io.*;
import java.sql.*;
import java.util.*;
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
	
	private static final List<String> CODES = Arrays.asList(new String[] {"AF", "DL"});

	private static final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat _tf = new SimpleDateFormat("HH:mm");

	protected void setUp() throws Exception {
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestInnovataGlobalLoad.class);
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader");

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

	/* Rules for adding a flight:
	 * 1. Range contains today.
	 * 2. Airline code is AF or DL and the codeshare field is blank.
	 * 3. Airline code is a codeshare (ie. NOT AF or DL) and the codeshare info field contains AF or DL.
	 */
	public void testLoadCSV() throws IOException, SQLException, ParseException {

		File f = new File("c:\\temp\\Sectorised Global Sample - Aug 01.csv");
		assertTrue(f.exists());
		
		// Write output file
		PrintWriter out = new PrintWriter("c:\\temp\\Aug01.csv");

		// Prepare the statement
		PreparedStatement ps = _c.prepareStatement("INSERT INTO INNOVATA_IMPORT VALUES (?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		// Load the File
		int rowCount = 0;
		int batchCount = 0;
		long now = System.currentTimeMillis();
		LineNumberReader lr = new LineNumberReader(new FileReader(f), 102400);
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
			boolean isOK = (now >= sd) && (now <=ed);
			
			// Check codeshare operation
			boolean isCS = "1".equals(entries.get(48));
			
			// Check the airline
			String code = entries.get(0).toUpperCase();
			boolean isMainLine = isOK && CODES.contains(code);
			boolean isCodeShare = false; 
			if (isOK && _aCodes.contains(code) && !isMainLine && !CODES.contains(code) && !CODES.contains(entries.get(3))) {
				String csInfo = entries.get(50);
				for (Iterator<String> i = CODES.iterator(); i.hasNext() && !isCodeShare; ) {
					String mlCode = i.next();
					isCodeShare |= (csInfo.indexOf(mlCode) != -1);
				}
			}

			// Add the entry
			if ((isMainLine || isCodeShare) && !isCS) {
				ps.clearParameters();
				ps.setInt(1, Integer.parseInt(entries.get(52)));
				ps.setString(2, entries.get(0));
				ps.setInt(3, StringUtils.parse(entries.get(1), 1));
				ps.setInt(4, StringUtils.parse(entries.get(46), 1));
				ps.setString(5, entries.get(27));
				ps.setDate(6, new java.sql.Date(sd));
				ps.setDate(7, new java.sql.Date(ed));
				ps.setString(8, entries.get(14));
				ps.setString(9, entries.get(22));
				ps.setTime(10, new Time(_tf.parse(entries.get(18)).getTime()));
				ps.setTime(11, new Time(_tf.parse(entries.get(23)).getTime()));
				ps.setInt(12, StringUtils.parse(entries.get(35), 0));
				ps.setString(13, entries.get(34));
				ps.setInt(14, StringUtils.parse(entries.get(36), 0));
				ps.setBoolean(15, isCS);
				ps.setString(16, entries.get(50));
				ps.setString(17, entries.get(3));

				// Add the entry
				try {
					ps.addBatch();
					batchCount++;
					out.println(data);
					rowCount++;
					if (batchCount == 40) {
						ps.executeBatch();
						batchCount = 0;
					}
				} catch (SQLException se) {
					log.error("Error adding entry on line " + lr.getLineNumber() + " - " + se.getMessage());
				}
				
				// Log data
				if ((rowCount % 100) == 0)
					log.info(rowCount + " entries added");
			}
			
			if ((lr.getLineNumber() % 100000) == 0)
				log.info(lr.getLineNumber() + " rows read");
		}
		
		// Update the database
		if (batchCount > 0) {
			try {
				ps.executeBatch();
			} catch (SQLException se) {
				log.error("Error adding entry on line " + lr.getLineNumber() + " - " + se.getMessage());
			}
		}

		// Close the streams
		ps.close();
		lr.close();
		out.close();
	}
	
	public void testLoadDAO() throws Exception {
		
		File f = new File("data/Aug01.csv");
		assertTrue(f.exists());
		
		// Load Airports
		GetAirport apdao = new GetAirport(_c);
		Collection<Airport> airports = apdao.getAll().values();
		assertNotNull(airports);
		assertFalse(airports.isEmpty());
		
		// Get the DAO
		GetFullSchedule dao = new GetFullSchedule(new FileInputStream(f));
		dao.setEffectiveDate(new java.util.Date());
		dao.setPrimaryCodes(Arrays.asList(new String[] {"DL", "AF"}));
		dao.setAirlines(_airlines);
		dao.setAirports(airports);
		
		// Load the legs
		dao.load();
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
	}
}