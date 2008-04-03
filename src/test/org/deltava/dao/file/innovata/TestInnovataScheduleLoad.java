// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.text.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.TZInfo;
import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.innovata.GetSchedule;

import org.deltava.util.StringUtils;

public class TestInnovataScheduleLoad extends TestCase {

	private static final Logger log = Logger.getLogger("TestInnovataSched");
	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");

	private static Map<String, Airport> _apMap = new HashMap<String, Airport>();
	private static Map<String, Airline> _alMap = new TreeMap<String, Airline>();
	
	private Date _effDate; 

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		_effDate = _df.parse("04/04/2006");

		// Load airlines
		if (_alMap.isEmpty()) {
			LineNumberReader lr = new LineNumberReader(new FileReader("data/airlines.csv"));
			while (lr.ready()) {
				StringTokenizer tkns = new StringTokenizer(lr.readLine(), ",");
				if (tkns.countTokens() != 3) {
					log.warn("Invalid Token Count on Line " + lr.getLineNumber() + ", count = " + tkns.countTokens()
							+ ", expected = 3");
				} else {
					Airline al = new Airline(tkns.nextToken(), tkns.nextToken());
					al.setActive("1".equals(tkns.nextToken()));
					_alMap.put(al.getCode(), al);
				}
			}

			lr.close();
			log.info("Loaded " + _alMap.size() + " airlines");
		}

		// Load airports
		if (_apMap.isEmpty()) {
			LineNumberReader lr = new LineNumberReader(new FileReader("data/airports.csv"));
			while (lr.ready()) {
				StringTokenizer tkns = new StringTokenizer(lr.readLine(), ",");
				if (tkns.countTokens() != 6) {
					log.warn("Invalid Token Count on Line " + lr.getLineNumber() + ", count = " + tkns.countTokens()
							+ ", expected = 6");
				} else {
					Airport a = new Airport(tkns.nextToken(), tkns.nextToken(), "");
					String tzName = tkns.nextToken();
					TZInfo tz = TZInfo.get(tzName);
					if (tz == null)
						tz = TZInfo.init(tzName, null, null);

					a.setTZ(tz);
					a.setName(tkns.nextToken());
					a.setLocation(Double.parseDouble(tkns.nextToken()), Double.parseDouble(tkns.nextToken()));

					_apMap.put(a.getIATA(), a);
					_apMap.put(a.getICAO(), a);
				}
			}

			lr.close();
		}
	}
	
	protected Collection<CSVTokens> loadTestData(String fName, String effDate) {

		// Get the data and the DAO
		try {
			GetSchedule dao = new GetSchedule(new FileInputStream("data/innovata/" + fName));
			
			dao.setEffectiveDate(_df.parse(effDate));
			Collection<CSVTokens> tkns = dao.load();
			assertNotNull(tkns);
			assertFalse(tkns.isEmpty());

			return tkns;
		} catch (FileNotFoundException fnfe) {
			fail("Cannot find data/innovata/" + fName);
		} catch (DAOException de) {
			fail(de.getMessage());
		} catch (ParseException pe) {
			fail("Invalid effective date - " + pe.getMessage());
		}

		return Collections.emptySet();
	}

	public void testValidTZ() {
		for (Iterator<Airport> i = _apMap.values().iterator(); i.hasNext();) {
			Airport a = i.next();
			if (a.getTZ() == null)
				log.warn(a.getIATA() + " has no timezone");

			assertNotNull(a.getTZ());
		}
	}
	
	public void testLoadSingleFlight() {
		GetSchedule dao = new GetSchedule(loadTestData("iv_dl5037.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
	
	public void testMultiLegStartsInFuture() {
		GetSchedule dao = new GetSchedule(loadTestData("iv_dl110.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
	
	public void testLegsInPast() {
		GetSchedule dao = new GetSchedule(loadTestData("iv_dl263.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
	
	public void testDuplicatePair() {
		GetSchedule dao = new GetSchedule(loadTestData("iv_dl5597.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
	
	public void testMultiStageMultiDay() {
		GetSchedule dao = new GetSchedule(loadTestData("iv_dl5029.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		Collection<ScheduleEntry> entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
		
		// Try second flight
		dao = new GetSchedule(loadTestData("iv_dl5328.csv", "04/04/2006"));
		dao.setEffectiveDate(_effDate);
		entries = dao.process();
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
	
	public void testLoadSchedule() throws Exception {
		
		// Get the data and the DAO
		GetSchedule dao = new GetSchedule(new FileInputStream("data/innovata/iv_directs.csv"));
		dao.setBufferSize(32768);
		dao.setEffectiveDate(_df.parse("06/14/2006"));
		dao.load();
		Collection<ScheduleEntry> entries = dao.process();
		
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
		
		// Write the entries
		PrintWriter pw = new PrintWriter(new FileWriter("c:\\temp\\sched_out.txt"));
		for (Iterator<ScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
			ScheduleEntry se = i.next();
			pw.print(se.getFlightCode());
			pw.print(",");
			pw.print(se.getAirportD().getIATA());
			pw.print(",");
			pw.print(StringUtils.format(se.getTimeD(), "HH:mm"));
			pw.print(",");
			pw.print(se.getAirportA().getIATA());
			pw.print(",");
			pw.print(StringUtils.format(se.getTimeA(), "HH:mm"));
			pw.print(",");
			pw.print(se.getEquipmentType());
			pw.print(",");
			pw.println(StringUtils.format((se.getLength() / 10.0), "#0.0"));
		}
		
		// Close and dump eqtypes
		pw.close();
	}
}