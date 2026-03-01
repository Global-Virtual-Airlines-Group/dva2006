package org.deltava.beans.stats;

import junit.framework.*;

import org.hansel.CoverageDecorator;

import org.deltava.beans.schedule.Airport;

public class TestRunwayLandingStats extends TestCase {
	
	private RunwayLandingStats _rsw;
	
	public static Test suite() {
		return new CoverageDecorator(RunwayLandingStats.class, new Class[] { RunwayLandingStats.class });
	}

	@Override
	protected void tearDown() throws Exception {
		_rsw = null;
		super.tearDown();
	}

	public void testProperties() {
		Airport a = new Airport("ATL", "KATL", "Atlanta GA");
		assertNotNull(a);
		_rsw = new RunwayLandingStats(a, "26L", 2026);
		_rsw.setDistance(1100, 25);
		_rsw.setVerticalSpeed(-220, 15);
		_rsw.setScore(86.5, 11.50);
		_rsw.setCount(10);
		assertEquals(a, _rsw.getAirport());
		assertEquals("26L", _rsw.getRunway());
		assertEquals(2026, _rsw.getYear());
		assertEquals(10, _rsw.getCount());
		assertEquals(1100, _rsw.getAverageDistance());
		assertEquals(25, _rsw.getDistanceSD());
		assertEquals(-220, _rsw.getAverageVerticalSpeed());
		assertEquals(15, _rsw.getVerticalSpeedSD());
		assertEquals(86.5, _rsw.getAverageScore(), 0.01);
		assertEquals(11.5, _rsw.getScoreSD(), 0.01);
	}
	
	public void testMerge() {
		Airport a = new Airport("ATL", "KATL", "Atlanta GA");
		assertNotNull(a);
		
		_rsw = new RunwayLandingStats(a, "26L", 2026);
		_rsw.setCount(50);
		_rsw.setDistance(100, 15);
		
		RunwayLandingStats rsw1 = new RunwayLandingStats(a, "26L", 2025);
		rsw1.setCount(30);
		rsw1.setDistance(110, 20);
		
		RunwayLandingStats rsw2 = _rsw.merge(rsw1);
		assertNotNull(rsw2);
		assertEquals(0, rsw2.getYear());
		assertSame(a, rsw2.getAirport());
		assertEquals(_rsw.getRunway(), rsw2.getRunway());
		assertEquals(_rsw.getCount() + rsw1.getCount(), rsw2.getCount());
		assertEquals(103, rsw2.getAverageDistance());
		assertEquals(18, rsw2.getDistanceSD());
	}
	
	public void testSingleMerge() {
		Airport a = new Airport("ATL", "KATL", "Atlanta GA");
		assertNotNull(a);
		
		_rsw = new RunwayLandingStats(a, "26L", 2026);
		_rsw.setDistance(1100, 0);
		_rsw.setVerticalSpeed(-220, 0);
		_rsw.setScore(86.5, 0);
		_rsw.setCount(1);
		
		RunwayLandingStats rsw1 = new RunwayLandingStats(a, "26L", 2025);
		assertNotNull(rsw1);
		assertEquals(0, rsw1.getCount());
		assertSame(_rsw, _rsw.merge(rsw1));
	}
}