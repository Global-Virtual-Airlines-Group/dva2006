package org.deltava.beans.stats;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestFlightStatsEntry extends TestCase {

	private FlightStatsEntry _e;

	public static Test suite() {
		return new CoverageDecorator(TestFlightStatsEntry.class, new Class[] { FlightStatsEntry.class });
	}

	@Override
	protected void tearDown() throws Exception {
		_e = null;
		super.tearDown();
	}

	public void testProperties() {
		_e = new FlightStatsEntry("CRJ-200", 10, 310, 5010);
		_e.setACARSLegs(8);
		assertEquals("CRJ-200", _e.getLabel());
		assertEquals(10, _e.getLegs());
		assertEquals(8, _e.getACARSLegs());
		assertEquals(310, _e.getHours(), 0.001);
		assertEquals(5010, _e.getDistance());
		assertEquals(31.0, _e.getAvgHours(), 0.001);
		assertEquals(501, _e.getAvgDistance(), 0.001);
		assertEquals(0.8, _e.getACARSPercent(), 0.00);
	}
	
	public void testZeroLegs() {
		_e = new FlightStatsEntry("CRJ-200", 0, 310, 5010);
		assertEquals(0.0, _e.getAvgHours(), 0.00);
		assertEquals(0.0, _e.getAvgDistance(), 0.00);
		assertEquals(0.0, _e.getACARSPercent(), 0.00);
	}
	
	public void testComparator() {
		_e = new FlightStatsEntry("CRJ-200", 10, 310, 5010);
		FlightStatsEntry e2 = new FlightStatsEntry("B757-200", 10, 310, 5010);
		
		assertTrue(_e.compareTo(e2) > 0);
		assertTrue(e2.compareTo(_e) < 0);
	}
}