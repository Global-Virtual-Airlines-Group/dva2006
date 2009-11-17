// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

public class TestFlightPromotionHelper extends TestCase {
	
	private Logger log;
	
	private final Airline _a = new Airline("DVA", "Delta Virtual Airlines");
	private final Airport _atl = new Airport("ATL", "KATL", "Atlanta-Hartsfield GA");
	private final Airport _dob = new Airport("MGE", "KMGE", "Atlanta-Dobbins GA");
	private final Airport _jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
	private final Airport _bos = new Airport("BOS", "KBOS", "Boston MA");
	
	private final EquipmentType eq1 = new EquipmentType("B727-200");
	private final EquipmentType eq2 = new EquipmentType("B737-800");
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(TestFlightPromotionHelper.class);
		log.info("Starting");
		
		// Load airports
		TZInfo.init("US/Eastern", null, null);
		_atl.setLocation(34.6404, -84.4269);
		_atl.setTZ("US/Eastern");
		_jfk.setLocation(40.6397, -73.7789);
		_jfk.setTZ("US/Eastern");
		_dob.setLocation(33.9153, -84.5161);
		_dob.setTZ("US/Eastern");
		_bos.setLocation(42.3642, -71.005);
		_bos.setTZ("US/Eastern");
		
		// Load equipment programs
		assertNotNull(eq1);
		eq1.setACARSPromotionLegs(true);
		eq1.setPromotionSwitchLength(750);
		eq1.setMinimum1XTime(1800);
		eq1.setMaximumAccelTime(300);
		eq1.setPromotionMinLength(150);
		assertNotNull(eq2);
		eq2.setACARSPromotionLegs(false);
		eq2.setPromotionSwitchLength(750);
		eq2.setMinimum1XTime(300);
		eq2.setMaximumAccelTime(3600);
		eq2.setPromotionMinLength(5);
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testPIREP() {
		
		FlightReport p1 = new FlightReport(_a, 1, 1);
		assertNotNull(p1);
		p1.setAirportD(_atl);
		p1.setAirportA(_jfk);
		
		assertFalse(p1.hasAttribute(FlightReport.ATTR_ACARS));
		assertTrue(p1.getDistance() > eq1.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq2.getPromotionMinLength());
	
		FlightPromotionHelper helper = new FlightPromotionHelper(p1);
		assertFalse(helper.canPromote(eq1));
		assertTrue(helper.canPromote(eq2));
	}
	
	public void testACARS() {
		
		ACARSFlightReport p1 = new ACARSFlightReport(_a, 1, 1);
		assertNotNull(p1);
		p1.setAirportD(_atl);
		p1.setAirportA(_jfk);
		p1.setAttribute(FlightReport.ATTR_ACARS, true);
		p1.setTime(1, 7200);
		
		assertTrue(p1.hasAttribute(FlightReport.ATTR_ACARS));
		assertTrue(p1.getDistance() > eq1.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq2.getPromotionMinLength());
		
		FlightPromotionHelper helper = new FlightPromotionHelper(p1);
		assertTrue(helper.canPromote(eq1));
		assertTrue(helper.canPromote(eq2));
	}
	
	public void testDistance() {
		
		ACARSFlightReport p1 = new ACARSFlightReport(_a, 1, 1);
		assertNotNull(p1);
		p1.setAirportD(_atl);
		p1.setAirportA(_dob);
		p1.setAttribute(FlightReport.ATTR_ACARS, true);
		p1.setTime(1, 1800);
		
		assertFalse(p1.getDistance() > eq1.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq2.getPromotionMinLength());
		
		FlightPromotionHelper helper = new FlightPromotionHelper(p1);
		assertFalse(helper.canPromote(eq1));
		assertTrue(helper.canPromote(eq2));
	}
	
	public void testExcessiveAccel() {
		
		ACARSFlightReport p1 = new ACARSFlightReport(_a, 1, 1);
		assertNotNull(p1);
		p1.setAirportD(_atl);
		p1.setAirportA(_jfk);
		p1.setAttribute(FlightReport.ATTR_ACARS, true);
		p1.setTime(1, 2600);
		p1.setTime(2, 600);
		
		assertEquals(715, p1.getDistance());
		
		assertTrue(p1.hasAttribute(FlightReport.ATTR_ACARS));
		assertTrue(p1.getDistance() > eq1.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq2.getPromotionMinLength());
		assertTrue(p1.getDistance() < eq1.getPromotionSwitchLength());
		assertTrue(p1.getDistance() < eq2.getPromotionSwitchLength());
		assertTrue(p1.getTime(2) > eq1.getMaximumAccelTime());
		assertFalse(p1.getTime(2) > eq2.getMaximumAccelTime());
		
		FlightPromotionHelper helper = new FlightPromotionHelper(p1);
		assertFalse(helper.canPromote(eq1));
		assertTrue(helper.canPromote(eq2));
	}
	
	public void testInsufficient1X() {
		
		ACARSFlightReport p1 = new ACARSFlightReport(_a, 1, 1);
		assertNotNull(p1);
		p1.setAirportD(_atl);
		p1.setAirportA(_bos);
		p1.setAttribute(FlightReport.ATTR_ACARS, true);
		p1.setTime(1, 600);
		p1.setTime(2, 2600);
		
		assertTrue(p1.hasAttribute(FlightReport.ATTR_ACARS));
		assertTrue(p1.getDistance() > eq1.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq2.getPromotionMinLength());
		assertTrue(p1.getDistance() > eq1.getPromotionSwitchLength());
		assertTrue(p1.getDistance() > eq2.getPromotionSwitchLength());
		assertTrue(p1.getTime(1) < eq1.getMinimum1XTime());
		assertFalse(p1.getTime(1) < eq2.getMinimum1XTime());
		
		FlightPromotionHelper helper = new FlightPromotionHelper(p1);
		assertFalse(helper.canPromote(eq1));
		assertTrue(helper.canPromote(eq2));
	}
}