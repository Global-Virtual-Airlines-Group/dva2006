package org.deltava.beans.schedule;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestFuelTanks extends TestCase {

	private static final String[] TANK_NAMES = {"Center", "Left Main", "Left Aux", "Left Tip", "Right Main", "Right Aux", "Right Tip", "Center 2", "Center 3", "External", "External 2" };
	
	public void testTankNames() {
		for (int x = 0; x < FuelTank.values().length; x++) {
			FuelTank t = FuelTank.get(TANK_NAMES[x]);
			assertNotNull(t);
		}
	}
	
	public void testTankIDs() {
		for (int x = 0; x < FuelTank.values().length; x++) {
			FuelTank t = FuelTank.values()[x];
			assertNotNull(t);
		}
	}
	
	public void testFailure() {
		try {
			FuelTank t = FuelTank.get("crap");
			assertNull(t);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) {
			// empty
		}
	}
}