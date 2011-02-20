// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;
import junit.framework.TestCase;

public class TestDuplicateCheck extends TestCase {

	public void testCIDCheck() {
		
		Controller c = new Controller(12345);
		c.setFirstName("Test");
		c.setLastName("User");
		c.setCallsign("ICAO_TWR");
		
		Pilot p = new Pilot(12345);
		p.setFirstName("Test");
		p.setLastName("User2");
		p.setCallsign("TEST2");
		
		assertEquals(0, c.compareTo(p));
		assertEquals(0, p.compareTo(c));
		assertTrue(c.equals(p));
		assertTrue(p.equals(c));
	}
	
	public void testATIS() {
		
		Controller c = new Controller(12345);
		c.setFirstName("Test");
		c.setLastName("User");
		c.setFacility(Facility.TWR);
		c.setCallsign("ICAO_TWR");
		
		Controller c2 = new Controller(12345);
		c2.setFirstName("Test");
		c2.setLastName("User");
		c2.setFacility(Facility.ATIS);
		c2.setCallsign("ICAO_ATIS");
		
		Controller c3 = new Controller(12345);
		c3.setFirstName("Test");
		c3.setLastName("User");
		c3.setFacility(Facility.TWR);
		c3.setCallsign("ICAO_TWR");

		assertFalse(c.equals(c2));
		assertTrue(c.equals(c3));
		assertFalse(c2.equals(c));

		Collection<Controller> ctrs = new LinkedHashSet<Controller>();
		assertTrue(ctrs.add(c));
		assertTrue(ctrs.add(c2));
		assertEquals(2, ctrs.size());
		assertFalse(ctrs.add(c3));
		assertEquals(2, ctrs.size());
	}
}