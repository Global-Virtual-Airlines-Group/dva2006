// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.GeocodeResult;

import junit.framework.TestCase;

public class TestGoogleGeocode extends TestCase {

	@SuppressWarnings("static-method")
	public void testGeocodeV3() throws Exception {
		
		GetGoogleGeocode gcdao = new GetGoogleGeocode();
		GeocodeResult geoCode = gcdao.getGeoData(new GeoPosition(37.9961, 23.7619));
		assertNotNull(geoCode);
		assertNotNull(geoCode.getCity());
		assertNotNull(geoCode.getCityState());
		assertNotNull(geoCode.getCountry());
		
		gcdao.reset();
		geoCode = gcdao.getGeoData(new GeoPosition(47.15, -122.305));
		assertNotNull(geoCode);
		assertNotNull(geoCode.getCity());
		assertNotNull(geoCode.getCityState());
		assertNotNull(geoCode.getCountry());
		
		gcdao.reset();
		geoCode = gcdao.getGeoData(new GeoPosition(32.9969, -80.1864));
		assertNotNull(geoCode);
		assertNotNull(geoCode.getCity());
		assertNotNull(geoCode.getCityState());
		assertNotNull(geoCode.getCountry());
		
		gcdao.reset();
		geoCode = gcdao.getGeoData(new GeoPosition(31.8211, 35.2217));
		assertNull(geoCode);
	}
}