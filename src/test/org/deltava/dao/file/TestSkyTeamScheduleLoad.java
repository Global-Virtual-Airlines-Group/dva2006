package org.deltava.dao.file;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class TestSkyTeamScheduleLoad extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";
	
	private Connection _c;
	private final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "14072");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		// Get EQ types
		GetAircraft acdao = new GetAircraft(_c);
		_acTypes.addAll(acdao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}
	
	@SuppressWarnings("static-method")
	public void testConvertPDF() throws Exception {
		
		File f = new File("C:\\Temp\\Skyteam_Timetable_Q2_2020.pdf");
		assertTrue(f.exists());
		
		File txtF = new File("C:\\Temp\\skyteam2020.txt");
		if (txtF.exists())
			return;
		
		try (InputStream is = new FileInputStream(f)) {
			GetPDFText prdao = new GetPDFText(is);
			prdao.setStartPage(5);
			String txt = prdao.getText();
			try (OutputStream os = new BufferedOutputStream(new FileOutputStream(txtF), 131072); PrintWriter pw = new PrintWriter(os)) {
				pw.write(txt);
			}
		}
	}

	public void testLoadRaw() throws Exception {
		
		File f = new File("C:\\Temp\\skyteam2020.txt");
		assertTrue(f.exists());
		
		Collection<RawScheduleEntry> rawEntries = new ArrayList<RawScheduleEntry>();
		try (InputStream is = new FileInputStream(f)) {
			GetSkyTeamSchedule dao = new GetSkyTeamSchedule(is);
			dao.setAircraft(_acTypes);
			dao.setAirlines(SystemData.getAirlines().values());
			rawEntries.addAll(dao.process());
		}
		
		assertFalse(rawEntries.isEmpty());
		
		/* SetSchedule rwdao = new SetSchedule(_c);
		rwdao.purgeRaw(ScheduleSource.SKYTEAM);
		for (RawScheduleEntry rse : rawEntries)
			rwdao.writeRaw(rse);
		
		_c.commit();
		log.info("Wrote " + rawEntries.size() + " raw schedule entries"); */
		
		// Get from the database
		final LocalDate today = LocalDate.now();
		GetRawSchedule rawdao = new GetRawSchedule(_c);
		Collection<RawScheduleEntry> todaysRaw = rawdao.load(ScheduleSource.SKYTEAM, today);
		assertNotNull(todaysRaw);
		assertFalse(todaysRaw.isEmpty());
		
		// Calculate the leg numbers
		Collection<ScheduleEntry> legEntries = ScheduleLegHelper.calculateLegs(todaysRaw).stream().map(rse -> rse.toToday(today)).collect(Collectors.toList());
		
		// Make sure there are no dupes
		Collection<ScheduleEntry> uniqueCheck = new LinkedHashSet<ScheduleEntry>(legEntries);
		assertNotNull(uniqueCheck);
		assertEquals(legEntries.size(), uniqueCheck.size());
	}
}