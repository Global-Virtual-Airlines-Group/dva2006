// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.List;

import org.apache.log4j.*;

import org.deltava.beans.schedule.GeoPosition;

import junit.framework.TestCase;

public class TestBeanUtils extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
	}
	
	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testChangedBean() {
		
		GeoPosition gp1 = new GeoPosition(45, -98, 100);
		GeoPosition gp2 = BeanUtils.clone(gp1);
		assertNotNull(gp2);
		gp2.setLatitude(55);
		assertFalse(gp2.getLatitude() == gp1.getLatitude());
		
		List<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(gp1, gp2);
		assertNotNull(delta);
		assertEquals(1, delta.size());
		assertEquals("latitude", delta.get(0).getName());
	}
	
	@SuppressWarnings("static-method")
	public void testUnchangedBean() {
		
		GeoPosition gp1 = new GeoPosition(45, -98, 100);
		GeoPosition gp2 = BeanUtils.clone(gp1);
		assertNotNull(gp2);
		
		List<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(gp1, gp2);
		assertNotNull(delta);
		assertTrue(delta.isEmpty());
	}
	
	@SuppressWarnings("static-method")
	public void testIgnoredFields() {

		GeoPosition gp1 = new GeoPosition(45, -98, 100);
		GeoPosition gp2 = BeanUtils.clone(gp1);
		assertNotNull(gp2);
		gp2.setLatitude(55);
		gp2.setAltitude(200);
		assertTrue(gp1.getAltitude() != gp2.getAltitude());

		List<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(gp1, gp2, "altitude");
		assertNotNull(delta);
		assertEquals(1, delta.size());
		assertEquals("latitude", delta.get(0).getName());
	}
	
	@SuppressWarnings("static-method")
	public void testNull() {
		
		GeoPosition a = null;
		assertNull(a);
		assertNull(BeanUtils.clone(a));
		
		GeoPosition gp1 = new GeoPosition(45, -98, 100);
		List<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(gp1, a);
		assertNotNull(delta);
		assertTrue(delta.isEmpty());
	}
}