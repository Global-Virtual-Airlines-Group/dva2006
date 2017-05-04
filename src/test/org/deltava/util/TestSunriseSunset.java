// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.time.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import junit.framework.TestCase;

public class TestSunriseSunset extends TestCase {

	@SuppressWarnings("static-method")
	public void testStandard() {
	
		GeoLocation loc = new GeoPosition(33.75, -84.4); // ATL
		ZonedDateTime dt = ZonedDateTime.of(2017, 5, 2, 10, 15, 0, 0, ZoneId.of("America/New_York"));
		System.out.println("Date is " + StringUtils.format(dt, "MM/dd/yyyy HH:mm:ss"));
		Instant sr = SunriseSunset.getSunrise(loc, dt.toInstant());
		assertNotNull(sr);
		
		ZonedDateTime zdt = ZonedDateTime.ofInstant(sr, ZoneId.of("America/New_York"));
		assertNotNull(zdt);
		System.out.println("Sunrise is " + StringUtils.format(zdt, "MM/dd/yyyy HH:mm:ss"));
		
		Instant ss = SunriseSunset.getSunset(loc, dt.toInstant());
		assertNotNull(ss);
		assertTrue(ss.isAfter(sr));
		ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ss, ZoneId.of("America/New_York"));
		assertNotNull(zdt2);
		System.out.println("Sunset is " + StringUtils.format(zdt2, "MM/dd/yyyy HH:mm:ss"));
	}
	
	public void testPolarWinter() {
		
	}
	
	public void testPolarSummer() {
		
	}
}