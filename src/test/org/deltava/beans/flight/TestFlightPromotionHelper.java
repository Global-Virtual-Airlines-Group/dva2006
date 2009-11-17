// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

public class TestFlightPromotionHelper extends TestCase {
	
	private Airport _atl = new Airport("ATL", "KATL", "Atlanta-Hartsfield GA");
	private Airport _dob = new Airport("MGE", "KMGE", "Atlanta-Dobbins GA");
	private Airport _jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		
		// Load airports
		TZInfo.init("US/Eastern", null, null);
		_atl.setLocation(34.6404, -84.4269);
		_atl.setTZ("US/Eastern");
		_jfk.setLocation(40.6397, -73.7789);
		_jfk.setTZ("US/Eastern");
		_dob.setLocation(33.9153, -84.5161);
		_dob.setTZ("US/Eastern");
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testPIREP() {
		
		EquipmentType eq1 = new EquipmentType("B727-200");
		assertNotNull(eq1);
		eq1.setACARSPromotionLegs(true);
		eq1.setMinimum1XTime(1800);
		eq1.setMaximumAccelTime(300);
		eq1.setPromotionMinLength(150);
	}
	
	public void testACARS() {
		
	}
	
	public void testDistance() {
		
	}
	
	public void testExcessiveAccel() {
		
	}
	
	public void testInsufficient1X() {
		
	}
}