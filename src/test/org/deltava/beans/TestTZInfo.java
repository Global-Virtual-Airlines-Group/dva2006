package org.deltava.beans;

import java.time.*;

import junit.framework.TestCase;

public class TestTZInfo extends TestCase {

	@SuppressWarnings("static-method")
	public void testEastern() {
		TZInfo.init("US/Eastern", "Eastern Time", "ET");
		
		TZInfo tz = TZInfo.get("US/Eastern");
		assertNotNull(tz);
		assertEquals("Eastern Time", tz.getName());
		assertEquals("ET", tz.getAbbr());
		assertEquals("US/Eastern", tz.getZone().getId());
		
		// Check DST
		assertNotNull(tz.getZone().getRules().nextTransition(Instant.now()));
		LocalDateTime lt = LocalDateTime.of(2022, 6, 1, 0, 0);
		assertTrue(tz.getZone().getRules().isDaylightSavings(lt.toInstant(ZoneOffset.UTC)));
		lt = LocalDateTime.of(2022, 12, 1, 0, 0);
		assertFalse(tz.getZone().getRules().isDaylightSavings(lt.toInstant(ZoneOffset.UTC)));
	}
	
	@SuppressWarnings("static-method")
	public void testNoDST() {
		TZInfo.init("America/St_Thomas", "Carribbean Time", "CBT");
		
		TZInfo tz = TZInfo.get("America/St_Thomas");
		assertNotNull(tz);
		assertEquals("Carribbean Time", tz.getName());
		assertEquals("CBT", tz.getAbbr());
		assertEquals("America/St_Thomas", tz.getZone().getId());
		
		// Check DST
		assertNull(tz.getZone().getRules().nextTransition(Instant.now()));
		LocalDateTime lt = LocalDateTime.of(2022, 6, 1, 0, 0);
		assertFalse(tz.getZone().getRules().isDaylightSavings(lt.toInstant(ZoneOffset.UTC)));
		lt = LocalDateTime.of(2022, 12, 1, 0, 0);
		assertFalse(tz.getZone().getRules().isDaylightSavings(lt.toInstant(ZoneOffset.UTC)));
	}
}