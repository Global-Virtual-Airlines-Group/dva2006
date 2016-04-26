// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.flight.ETOPS;
import org.deltava.beans.flight.ETOPSHelper;

import org.deltava.util.GeoUtils;

import junit.framework.TestCase;

public class TestETOPSHelper extends TestCase {
	
	private Airport _jfk;
	private Airport _dkr;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		_jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
		_jfk.setLocation(40.6392, -73.7789);
		
		_dkr = new Airport("DKR", "GOOY", "Dakar Senegal");
		_dkr.setLocation(14.7397, -17.4889);
		
		Collection<Airport> airports = new ArrayList<Airport>();
		airports.add(_jfk);
		airports.add(_dkr);
		ETOPSHelper.init(airports);
	}
	
	public void testGC() {
		
		Collection<GeoLocation> gc = GeoUtils.greatCircle(_jfk, _dkr, 25);
		ETOPS e = ETOPSHelper.classify(gc).getResult();
		assertNotNull(e);
	}
}