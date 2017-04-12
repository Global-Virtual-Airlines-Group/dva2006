package org.deltava.util;

import java.util.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("static-method")
public class TestGeoUtils extends TestCase {

	public void testGreatCircle() {
		GeoPosition ogg = new GeoPosition(20.8986, -156.4305);
		GeoPosition lax = new GeoPosition(33.9425, -118.4080);
		
		List<GeoLocation> gc = GeoUtils.greatCircle(ogg, lax, 90);
		Iterator<?> i = gc.iterator();
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
		assertEquals(116, GeoUtils.normalize(836 + (360 * 1000000)), 0.0);
	}
	
	public void testBearingPoint() {
		GeoLocation l1 = new GeoPosition(0.00, 0.00);
		
		GeoLocation l2 = GeoUtils.bearingPoint(l1, GeoLocation.DEGREE_MILES, 90);
		assertEquals(GeoLocation.DEGREE_MILES, GeoUtils.distance(l1, l2), 0.5);
		GeoLocation l3 = GeoUtils.bearingPoint(l1, 15, -40);
		assertEquals(15, GeoUtils.distance(l1, l3), 0.5);
	}
	
	public void testCourse() {
		GeoPosition loc = new GeoPosition(33.6347, -84.4361);
		GeoPosition r28 = new GeoPosition(33.631822,-84.4184);
		GeoPosition r27r = new GeoPosition(33.634703,-84.408908);
		
		double brg1 = GeoUtils.course(r28, loc);
		double brg11 = GeoUtils.course(loc, r28);
		assertEquals(brg1-180, brg11, 0.016);
		double brg2 = GeoUtils.course(r27r, loc);
		double brg21 = GeoUtils.course(loc, r27r);
		assertEquals(brg2-180, brg21, 0.016);
	}
	
	public void testMeridianLatitude() {
		
		GeoLocation jfk = new GeoPosition(40+(48/60.0), (73+(47/60.0)) * -1);
		GeoLocation lax = new GeoPosition(33+(57/60.0), (118+(24/60.0)) * -1);
		GeoLocation hkg = new GeoPosition(22.32830, 114.19400);
		GeoLocation arn = new GeoPosition(59.6564, 17.9331);
		
		double lat = GeoUtils.meridianLatitude(jfk, lax, -111);
		double lat2 = GeoUtils.meridianLatitude(lax, jfk, -111);
		
		assertEquals(Math.toDegrees(0.635200), lat, 0.1);
		assertEquals(Math.toDegrees(0.635200), lat2, 0.1);
		
		double lat3 = GeoUtils.meridianLatitude(jfk, lax, -179.9);
		assertTrue(lat3 < Math.min(jfk.getLatitude(), lax.getLatitude()));
		
		assertFalse(GeoUtils.crossesMeridian(jfk, lax, -179));
		assertFalse(GeoUtils.crossesMeridian(jfk, lax, -19));
		assertTrue(GeoUtils.crossesMeridian(jfk, lax, -111));
		
		assertFalse(GeoUtils.crossesMeridian(jfk, lax, 11));
		
		assertTrue(GeoUtils.crossesMeridian(lax, hkg, -179.9));
		assertTrue(GeoUtils.crossesMeridian(lax, hkg, 179.9));
		assertFalse(GeoUtils.crossesMeridian(jfk, hkg, 11));
		
		assertTrue(GeoUtils.crossesMeridian(jfk, arn, -30));
	}
	
	public void testFormat() {
		
		GeoLocation nat5020 = new GeoPosition(50.0, 20.0);
		String fmt9 = GeoUtils.formatFS9(nat5020);
		String fmtX = GeoUtils.formatFSX(nat5020);
		assertNotNull(fmt9);
		assertNotNull(fmtX);
	}
	
	public void testXACARS() {
		GeoLocation loc = GeoUtils.parseXACARS("N40 37.3453 W73 47.117");
		assertNotNull(loc);
	}
	
	public void testIsValid() {
		assertTrue(GeoUtils.isValid(new GeoPosition(1, 1)));
		assertFalse(GeoUtils.isValid(new GeoPosition(0, 0)));
		assertTrue(GeoUtils.isValid(new GeoPosition(0, 0, 1)));
	}
	
	public void testGeometry() {
		
		List<GeoLocation> locs = Arrays.asList(new GeoPosition(40, -110), new GeoPosition(40, -111), new GeoPosition(39, -111), new GeoPosition(39, -110));
		
		Geometry g = GeoUtils.toGeometry(locs);
		assertNotNull(g);
		assertEquals(locs.size() + 1, g.getCoordinates().length);
		
		List<GeoLocation> locs2 = new ArrayList<GeoLocation>(GeoUtils.fromGeometry(g));
		assertNotNull(locs2);
		assertEquals(locs.size() + 1, locs2.size());
		for (int x = 0; x < locs.size(); x++)
			assertTrue(locs.get(x).equals(locs2.get(x)));
	}
}