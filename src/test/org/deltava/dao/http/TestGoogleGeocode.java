// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.GeocodeResult;

import junit.framework.TestCase;

public class TestGoogleGeocode extends TestCase {

	public void testGeocodeV3() throws Exception {
		
		GetGoogleGeocode gcdao = new GetGoogleGeocode();
		GeocodeResult geoCode = gcdao.getGeoData(new GeoPosition(37.9961, 23.7619));
		assertNotNull(geoCode);
	}
}
