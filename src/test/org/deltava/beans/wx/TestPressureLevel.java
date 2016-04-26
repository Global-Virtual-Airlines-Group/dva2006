// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import junit.framework.TestCase;

public class TestPressureLevel extends TestCase {

	@SuppressWarnings("static-method")
	public void testClosest() {
		
		assertEquals(PressureLevel.LOW, PressureLevel.getClosest(7000));
		assertEquals(PressureLevel.JET, PressureLevel.getClosest(36000));
		assertEquals(PressureLevel.HIGH, PressureLevel.getClosest(41000));
	}
}