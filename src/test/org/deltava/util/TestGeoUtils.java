// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.GeoPosition;

public class TestGeoUtils extends TestCase {

	public static Test suite() {
		return new CoverageDecorator(TestGeoUtils.class, new Class[] { GeoUtils.class });
	}
	
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
}