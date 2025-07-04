package org.deltava.beans.stats;

import java.time.*;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.hansel.CoverageDecorator;

public class TestAirlineTotals extends AbstractBeanTestCase {
	
	private AirlineTotals _t;
	
	public static Test suite() {
		return new CoverageDecorator(TestAirlineTotals.class, new Class[] { AirlineTotals.class });
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_t = new AirlineTotals(Instant.now());
		setBean(_t);
	}

	@Override
	protected void tearDown() throws Exception {
		_t = null;
		super.tearDown();
	}

	public void testProperties() {
		checkProperty("YTDHours", Double.valueOf(100.0));
		checkProperty("YTDLegs", Integer.valueOf(52));
		checkProperty("YTDMiles", Integer.valueOf(5311));
		checkProperty("MTDHours", Double.valueOf(101.0));
		checkProperty("MTDLegs", Integer.valueOf(53));
		checkProperty("MTDMiles", Integer.valueOf(5312));
		checkProperty("OnlineMiles", Long.valueOf(131250));
		checkProperty("OnlineHours", Double.valueOf(31100.0));
		checkProperty("OnlineLegs", Integer.valueOf(1552));
		checkProperty("ACARSHours", Double.valueOf(105.0));
		checkProperty("ACARSLegs", Integer.valueOf(1253));
		checkProperty("ACARSMiles", Integer.valueOf(255312));
		checkProperty("TotalMiles", Long.valueOf(231250));
		checkProperty("TotalHours", Double.valueOf(41100.0));
		checkProperty("TotalLegs", Integer.valueOf(2552));
		checkProperty("TotalPilots", Integer.valueOf(2510));
		checkProperty("ActivePilots", Integer.valueOf(714));
		checkProperty("DBRows", Integer.valueOf(163123));
		checkProperty("DBSize", Long.valueOf(12163123));
	}
	
	public void testValidation() {
		validateInput("YTDHours", Double.valueOf(-1), IllegalArgumentException.class);
		validateInput("YTDLegs", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("MTDHours", Double.valueOf(-1), IllegalArgumentException.class);
		validateInput("MTDLegs", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("OnlineMiles", Long.valueOf(-1), IllegalArgumentException.class);
		validateInput("OnlineHours", Double.valueOf(-1), IllegalArgumentException.class);
		validateInput("OnlineLegs", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("ACARSMiles", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("ACARSHours", Double.valueOf(-1), IllegalArgumentException.class);
		validateInput("ACARSLegs", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("TotalMiles", Long.valueOf(-1), IllegalArgumentException.class);
		validateInput("TotalHours", Double.valueOf(-1), IllegalArgumentException.class);
		validateInput("TotalLegs", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("TotalPilots", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("ActivePilots", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("DBSize", Long.valueOf(-1), IllegalArgumentException.class);
		validateInput("DBRows", Integer.valueOf(-1), IllegalArgumentException.class);
	}
	
	@SuppressWarnings("static-method")
	public void testAirlineAge() {
		ZonedDateTime zdt = ZonedDateTime.of(2001, 6, 12, 0, 0, 0, 0, ZoneOffset.UTC);
		AirlineTotals at2 = new AirlineTotals(zdt.plusDays(2).toInstant());
		assertEquals(2, at2.getAge());
	}
	
	public void testComparator() {
		AirlineTotals at2 = new AirlineTotals(_t.getEffectiveDate().plusMillis(100));
		assertTrue(at2.compareTo(_t) > 0);
		assertTrue(_t.compareTo(at2) < 0);
	}
}