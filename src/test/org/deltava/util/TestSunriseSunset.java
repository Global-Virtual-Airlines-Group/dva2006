// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.time.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import junit.framework.TestCase;

public class TestSunriseSunset extends TestCase {

	@SuppressWarnings("static-method")
	public void testStandard() {
		
		GeoLocation loc = new GeoPosition(34.060220, -84.454050);
		ZonedDateTime zdt = ZonedDateTime.of(2017, 5, 24, 12, 0, 0, 0, ZoneId.of("America/New_York"));
		
		ZonedDateTime sr = SunriseSunset.getSunrise(loc, zdt);
		assertNotNull(sr);
		System.out.println("Sunrise = " + sr);
		
		ZonedDateTime ss = SunriseSunset.getSunset(loc, zdt);
		assertNotNull(ss);
		System.out.println("Sunset = " + ss);
		assertTrue(ss.isAfter(sr));
	}
	
	@SuppressWarnings("static-method")
	public void testArctic() {
		
		GeoLocation loc = new GeoPosition(71.284992, -156.9219569);
		ZonedDateTime zdt = ZonedDateTime.of(2017, 5, 10, 12, 0, 0, 0, ZoneId.of("America/Anchorage"));
		ZonedDateTime sr = SunriseSunset.getSunrise(loc, zdt);
		assertNotNull(sr);
		System.out.println("Sunrise = " + sr);
		
		ZonedDateTime ss = SunriseSunset.getSunset(loc, zdt);
		assertNotNull(ss);
		System.out.println("Sunset = " + ss);
		assertFalse(ss.isAfter(sr)); // Sunset (from the previous day) occurs right before sunrise

		// 24-hour daylight
		zdt = ZonedDateTime.of(2017, 5, 24, 12, 0, 0, 0, ZoneId.of("America/Anchorage"));
		sr = SunriseSunset.getSunrise(loc, zdt);
		assertNull(sr);
		ss = SunriseSunset.getSunset(loc, zdt);
		assertNull(ss);
		
		// Only one sunrise, start of 24-hour daylight
		zdt = ZonedDateTime.of(2017, 5, 14, 12, 0, 0, 0, ZoneId.of("America/Anchorage"));
		sr = SunriseSunset.getSunrise(loc, zdt);
		assertNotNull(sr);
		System.out.println("Sunrise = " + sr);
		ss = SunriseSunset.getSunset(loc, zdt);
		assertNull(ss);
		
		// Only one sunset, start of 24-hour darkness
		zdt = ZonedDateTime.of(2017, 11, 15, 12, 0, 0, 0, ZoneId.of("America/Anchorage"));
		sr = SunriseSunset.getSunrise(loc, zdt);
		System.out.println("Sunrise = " + sr);
		ss = SunriseSunset.getSunset(loc, zdt);
		System.out.println("Sunset = " + ss);
	}
	
	@SuppressWarnings("static-method")
	public void testAntarctic() {
		
		GeoLocation loc = new GeoPosition(-77.85, 166.666667);
		ZonedDateTime zdt = ZonedDateTime.of(2017, 5, 24, 12, 0, 0, 0, ZoneId.of("Antarctica/McMurdo"));

		// Currently 24 hour night
		ZonedDateTime sr = SunriseSunset.getSunrise(loc, zdt);
		assertNull(sr);
		ZonedDateTime ss = SunriseSunset.getSunset(loc, zdt);
		assertNull(ss);
	}
}