package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import junit.framework.TestCase;

public class TestAirspace extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void testContains() {
		
		List<GeoLocation> locs = Arrays.asList(new GeoPosition(40, -110), new GeoPosition(40, -111), new GeoPosition(39, -111), new GeoPosition(39, -110));
		
		Airspace a = new Airspace("P-1", AirspaceType.P);
		a.setBorder(locs);
		assertFalse(a.contains(new GeoPosition(41, -111)));
		assertTrue(a.contains(new GeoPosition(39.75, -110.5)));
	}
	
	public static void testDistanceTo() {
		
		List<GeoLocation> locs = Arrays.asList(new GeoPosition(40, -110), new GeoPosition(40, -111), new GeoPosition(39, -111), new GeoPosition(39, -110));
		GeospaceLocation gl = new GeoPosition(39.75, -110.5);
		
		Airspace a = new Airspace("P-1", AirspaceType.P);
		a.setBorder(locs);
		assertTrue(a.contains(gl));

		// Load into prohibited
		Airspace.init(Collections.singleton(a));
		
		Airspace a2 = Airspace.isRestricted(gl);
		assertNotNull(a2);
		assertEquals(a.getID(), a2.getID());
		
		Airspace.findRestricted(gl, 5);
	}
}