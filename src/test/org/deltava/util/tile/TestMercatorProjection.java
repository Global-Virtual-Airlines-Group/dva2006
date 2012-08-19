package org.deltava.util.tile;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

public class TestMercatorProjection extends TestCase {
	
	private MercatorProjection mp;
	
	protected void tearDown() throws Exception {
		mp = null;
		super.tearDown();
	}

	public void testInteroperability() {
		mp = new MercatorProjection(10);
		for (int lat = 85; lat > MercatorProjection.MIN_LATITUDE; lat-- ) {
			for (int lng = -180; lng < 180; lng++) {
				System.out.println("lat = " + lat + " lng = " + lng);
				java.awt.Point p = mp.getPixelAddress(new GeoPosition(lat, lng));
				GeoLocation loc = mp.getGeoPosition(p.x, p.y);
				assertEquals(lat, loc.getLatitude(), 0.0015);
				assertEquals(lng, loc.getLongitude(), 0.0015);
			}
		}
	}
}