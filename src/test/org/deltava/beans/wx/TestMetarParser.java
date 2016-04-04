// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.*;
import org.deltava.beans.flight.ILSCategory;

import junit.framework.TestCase;

public class TestMetarParser extends TestCase {
	
	private Logger log;
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private LineNumberReader lr;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestMetarParser.class);
		
		File f = new File("data/metars.txt");
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
			else if (data.startsWith("200") || data.startsWith("201")) {
				dt = df.parse(data);
				assertNotNull(dt);
				continue;
			}
			
			// Parse the METAR
			try {
				METAR m = MetarParser.parse(data);
				assertNotNull(m);
				log.info("Processed " + m.getCode());
			} catch (Exception e) {
				log.warn(data);
				throw e;
			}
		}
	}
	
	public void testCloudHeight() {
		
		METAR m = MetarParser.parse("BIKF 232030Z 24018KT 8000 -SHRASN FEW014CB SCT028 BKN046 02/M01 Q0989 20710056 ");
		assertNotNull(m);
		assertEquals(3, m.getClouds().size());
		CloudLayer cl = m.getClouds().first();
		assertEquals(1400, cl.getHeight());
	}
	
	public void testVisibility() {
		
		METAR m = MetarParser.parse("BIKF 232030Z 24018KT 8000 -SHRASN FEW014CB SCT028 BKN046 02/M01 Q0989 20710056 ");
		assertNotNull(m);
		assertEquals(Distance.METERS.getFeet(8000), m.getVisibility(), 0.01);
		
		METAR m2 = MetarParser.parse("CYBG 240000Z 11006KT 15SM SKC M06/M09 A3004 RMK SLP183");
		assertNotNull(m2);
		assertEquals(Distance.SM.getFeet(15), m2.getVisibility(), 0.01);
		
		METAR m3 = MetarParser.parse("AYMH 240000Z 09008KT 9999 SCT025 BKN140 22/16 Q1020");
		assertNotNull(m3);
		ILSCategory ils = WeatherUtils.getILS(m3);
		assertEquals(ILSCategory.CATI, ils);
	}
	
	public void testBadMETARs()  {
		
		METAR m = MetarParser.parse("UBEE 041600Z 23008KT CAVOK 13/03 Q1019 R30CLRD// NOSIG");
		assertNotNull(m);
		
		m = MetarParser.parse("LUKK 041600Z 23004KT CAVOK 19/04 Q1012 R08/D NOSIG");
		assertNotNull(m);
	}
}