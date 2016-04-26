package org.deltava.util.tile;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

public class TestMercatorProjection extends TestCase {
	
	private MercatorProjection mp;
	
	@Override
	protected void tearDown() throws Exception {
		mp = null;
		super.tearDown();
	}

	public void testInteroperability() {
		mp = new MercatorProjection(10);
		for (int lat = 85; lat > MercatorProjection.MIN_LATITUDE; lat-- ) {
			for (int lng = -180; lng < 180; lng++) {
				//System.out.println("lat = " + lat + " lng = " + lng);
				java.awt.Point p = mp.getPixelAddress(new GeoPosition(lat, lng));
				GeoLocation loc = mp.getGeoPosition(p.x, p.y);
				assertEquals(lat, loc.getLatitude(), 0.0015);
				assertEquals(lng, loc.getLongitude(), 0.0015);
			}
		}
	}
	
	public void testTileNWAddress() {
		mp = new MercatorProjection(5);
		GeoLocation loc = new GeoPosition(MercatorProjection.MAX_LATITUDE - 0.25, -179.98);
		java.awt.Point pt = mp.getPixelAddress(loc);
		assertNotNull(pt);
		TileAddress addr = mp.getAddress(loc);
		assertNotNull(addr);
		assertEquals(0, addr.getX());
		assertEquals(0, addr.getY());
	}
	
	public void testTileSEAddress() {
		mp = new MercatorProjection(5);
		GeoLocation loc = new GeoPosition(-MercatorProjection.MAX_LATITUDE + 0.25, 179.98);
		java.awt.Point pt = mp.getPixelAddress(loc);
		assertNotNull(pt);
		TileAddress addr = mp.getAddress(loc);
		assertNotNull(addr);
		assertEquals(31, addr.getX());
		assertEquals(31, addr.getY());
	}
}