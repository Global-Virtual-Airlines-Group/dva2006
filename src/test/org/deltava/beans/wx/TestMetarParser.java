// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

public class TestMetarParser extends TestCase {
	
	private Logger log;
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private LineNumberReader lr;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestMetarParser.class);
		
		File f = new File("data/metar-20z.txt");
		assertTrue(f.exists());
		
		lr = new LineNumberReader(new FileReader(f));
		assertNotNull(lr);
	}

	protected void tearDown() throws Exception {
		log.info("Processed to Line #" + lr.getLineNumber());
		lr.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testMETARs() throws Exception {
		Date dt = null;
		
		while (lr.ready()) {
			String data = lr.readLine();
			if (data.length() < 3)
				continue;
			else if (data.startsWith("200")) {
				dt = df.parse(data);
				continue;
			}
			
			// Parse the METAR
			METAR m = MetarParser.parse(data);
			assertNotNull(m);
			assertEquals(dt, m.getDate());
			log.info("Processed " + m.getCode());
		}
	}
}