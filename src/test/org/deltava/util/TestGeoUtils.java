package org.deltava.util;

import java.util.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

public class TestGeoUtils extends TestCase {

	public void testGreatCircle() {
		GeoPosition ogg = new GeoPosition(20.8986, -156.4305);
		GeoPosition lax = new GeoPosition(33.9425, -118.4080);
		
		List gc = GeoUtils.greatCircle(ogg, lax, 90);
		Iterator i = gc.iterator();
		GeoPosition gp = (GeoPosition) i.next(); 
		while (i.hasNext()) {
			GeoPosition gp2 = (GeoPosition) i.next();
			assertTrue(gp.distanceTo(gp2) < 100);
			gp = gp2;
		}
	}
	
	public void testNormalize() {
		assertEquals(1, GeoUtils.normalize(361), 0.0);
		assertEquals(116, GeoUtils.normalize(116), 0.0);
		assertEquals(359, GeoUtils.normalize(-1), 0.0);
		assertEquals(116, GeoUtils.normalize(476), 0.0);
		assertEquals(116, GeoUtils.normalize(836), 0.0);
	}
	
	public void testBearingPoint() {
		GeoLocation l1 = new GeoPosition(0.00, 0.00);
		
		GeoLocation l2 = GeoUtils.bearingPoint(l1, GeoLocation.DEGREE_MILES, 90);
		assertEquals(GeoLocation.DEGREE_MILES, GeoUtils.distance(l1, l2), 0.5);
		GeoLocation l3 = GeoUtils.bearingPoint(l1, 15, -40);
		assertEquals(15, GeoUtils.distance(l1, l3), 0.5);
	}
	
	public void testDegreeCharacter() throws Exception {
		String s = new String("°".getBytes(), "CP1252");
		int cp = s.codePointAt(0);
		assertEquals(176, cp);
		char c = s.charAt(0);
		assertEquals(176, c);
	}
}