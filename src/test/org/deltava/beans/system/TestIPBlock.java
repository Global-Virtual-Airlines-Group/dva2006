// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import junit.framework.TestCase;

public class TestIPBlock extends TestCase {

	public void testProperties() {
		IPBlock b = new IPBlock("208.89.88.0/19");
		assertNotNull(b);
		assertEquals("208.89.88.0/19", b.toString());
		assertEquals(19, b.getBits());
		assertEquals("208.89.88.0", b.getAddress());
	}
	
	public void testContains() {
		IPBlock b = new IPBlock("208.89.88.0/19");
		assertNotNull(b);
		assertEquals(8192, b.getSize());
		assertTrue(b.contains("208.89.100.89"));
	}
}