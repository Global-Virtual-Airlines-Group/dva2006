// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.TZInfo;
import org.deltava.dao.file.GetSchedule;

public class TestInnovataScheduleLoad extends TestCase {

	private Logger log = Logger.getLogger("TestInnovataSched");

	private static Map<String, Airport> _apMap = new HashMap<String, Airport>();
	private static Map<String, Airline> _alMap = new TreeMap<String, Airline>();

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");

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

	public void testValidTZ() {
		for (Iterator<Airport> i = _apMap.values().iterator(); i.hasNext();) {
			Airport a = i.next();
			if (a.getTZ() == null)
				log.warn(a.getIATA() + " has no timezone");

			assertNotNull(a.getTZ());
		}
	}
	
	public void testLoadSchedule() throws Exception {
		
		// Get the data and the DAO
		InputStream is = new FileInputStream("data/iv_directs.csv");
		GetSchedule dao = new GetSchedule(is, _alMap.values(), _apMap.values());
		dao.load();
		Collection<ScheduleEntry> entries = dao.process();
		
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		log.info("Loaded " + entries.size() + " entries");
	}
}