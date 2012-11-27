// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import junit.framework.TestCase;

public class TestIPBlock extends TestCase {

	public void testProperties() {
		IPBlock b = new IPBlock(0, "108.204.16.0", "108.204.17.255", 23);
		assertNotNull(b);
		assertEquals("108.204.16.0/23", b.toString());
		assertEquals(23, b.getBits());
		assertEquals("108.204.16.0", b.getAddress());
	}
	
	public void testContains() {
		IPBlock b = new IPBlock(0, "108.204.16.0", "108.204.17.255", 23);
		assertNotNull(b);
		assertEquals(2 << (9-1), b.getSize());
		assertTrue(b.contains("108.204.16.13"));
	}
}