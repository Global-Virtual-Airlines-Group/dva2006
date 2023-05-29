// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.*;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.TZInfo;

import org.deltava.comparators.AirportComparator;

public class TestScheduleLoad extends TestCase {

	private Logger log = LogManager.getLogger(TestScheduleLoad.class);

	private static Map<String, Airport> _apMap = new HashMap<String, Airport>();
	private static Map<String, Airline> _alMap = new TreeMap<String, Airline>();
	private static Set<Airport> _airports;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());

		// Load airports
		if (_apMap.isEmpty()) {
			try (LineNumberReader lr = new LineNumberReader(new FileReader("data/airports.csv"))) {
				while (lr.ready()) {
					StringTokenizer tkns = new StringTokenizer(lr.readLine(), ",");
					if (tkns.countTokens() != 6) {
						log.warn("Invalid Token Count on Line " + lr.getLineNumber() + ", count = " + tkns.countTokens() + ", expected = 6");
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
			}

			// Create an airport set
			_airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.IATA));
			_airports.addAll(_apMap.values());
			log.info("Loaded " + _airports.size() + " airports");
		}

		// Load airlines
		if (_alMap.isEmpty()) {
			try (LineNumberReader lr = new LineNumberReader(new FileReader("data/airlines.csv"))) {
			while (lr.ready()) {
				StringTokenizer tkns = new StringTokenizer(lr.readLine(), ",");
				if (tkns.countTokens() != 3) {
					log.warn("Invalid Token Count on Line " + lr.getLineNumber() + ", count = " + tkns.countTokens() + ", expected = 3");
				} else {
					Airline al = new Airline(tkns.nextToken(), tkns.nextToken());
					al.setActive("1".equals(tkns.nextToken()));
					_alMap.put(al.getCode(), al);
				}
			}

			log.info("Loaded " + _alMap.size() + " airlines");
			}
		}
	}

	private List<List<String>> loadScheduleCSV(String fName) throws IOException {
		List<List<String>> results = new LinkedList<List<String>>();
		try (LineNumberReader lr = new LineNumberReader(new FileReader("data/" + fName), 65536)) {
			while (lr.ready()) {
				StringTokenizer tkns = new StringTokenizer(lr.readLine(), ",");
				if (tkns.countTokens() < 9) {
					log.warn("Invalid Token Count on Line " + lr.getLineNumber() + ", count = " + tkns.countTokens() + ", expected = 9");
				} else {
					List<String> entry = new ArrayList<String>();
					while (tkns.hasMoreTokens())
						entry.add(tkns.nextToken());

					results.add(entry);
				}
			}
		}

		log.info("Loaded " + results.size() + " lines from data/" + fName);
		return results;
	}

	public void testValidTZ() {
		for (Iterator<Airport> i = _airports.iterator(); i.hasNext();) {
			Airport a = i.next();
			if (a.getTZ() == null)
				log.warn(a.getIATA() + " has no timezone");

			assertNotNull(a.getTZ());
		}
	}

	public void testValidCodes() throws IOException {
		List<List<String>> sched = loadScheduleCSV("dva_sched.csv");
		for (Iterator<List<String>> i = sched.iterator(); i.hasNext();) {
			List<String> entry = i.next();
			assertEquals(9, entry.size());
			String adCode = entry.get(4);
			String aaCode = entry.get(6);
			if (!_apMap.containsKey(adCode)) {
				log.warn("Cannot find " + adCode);
			} else if (!_apMap.containsKey(aaCode)) {
				log.warn("Cannot find " + aaCode);
			}

			assertTrue(_apMap.containsKey(adCode));
			assertTrue(_apMap.containsKey(aaCode));
		}
	}

	public void testTimes() throws IOException {
		List<List<String>> sched = loadScheduleCSV("afv_schedule.csv");
		DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("hh:mm aa").parseLenient().toFormatter();
		for (Iterator<List<String>> i = sched.iterator(); i.hasNext();) {
			List<String> l = i.next();
			assertEquals(9, l.size());
			String[] entry = l.toArray(new String[0]);

			// Load the schedule entry
			ScheduleEntry se = new ScheduleEntry(_alMap.get(entry[0]), Integer.parseInt(entry[1]), Integer.parseInt(entry[2]));
			se.setEquipmentType(entry[3]);
			se.setAirportD(_apMap.get(entry[4]));
			se.setTimeD(LocalDateTime.parse(entry[5], df));
			se.setAirportA(_apMap.get(entry[6]));
			se.setTimeA(LocalDateTime.parse(entry[7], df));

			// Compare the times
			int schedTime = Integer.parseInt(entry[8]);
			int ln = se.getLength();
			if (ln != schedTime) {
				float hrs = (ln / 10.0f);
				int avgSpeed = Math.round(se.getDistance() / hrs);
				if ((avgSpeed < 350) || (avgSpeed > 510))
					log.warn("Warning on Flight " + se.getFlightCode() + " distance=" + se.getDistance() + " avgSpeed=" + avgSpeed);
			}
		}
	}
}