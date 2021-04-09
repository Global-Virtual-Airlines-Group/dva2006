package org.deltava.beans.flight;

import junit.framework.TestCase;

public class TestETOPS extends TestCase {
	
	@SuppressWarnings("static-method")
	public void testTime() {
		assertEquals(60, ETOPS.ETOPS60.getTime());
		assertEquals(120, ETOPS.ETOPS120.getTime());
	}
	
	@SuppressWarnings("static-method")
	public void testDistance() {
		assertEquals(448, ETOPS.ETOPS60.getRange());
		assertEquals(895, ETOPS.ETOPS120.getRange());
	}
}