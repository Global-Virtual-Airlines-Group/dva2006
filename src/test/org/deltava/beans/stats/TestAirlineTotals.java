package org.deltava.beans.stats;

import java.util.*;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.hansel.CoverageDecorator;

public class TestAirlineTotals extends AbstractBeanTestCase {
	
	private AirlineTotals _t;
	
	public static Test suite() {
		return new CoverageDecorator(TestAirlineTotals.class, new Class[] { AirlineTotals.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		_t = new AirlineTotals(System.currentTimeMillis());
		setBean(_t);
	}

	protected void tearDown() throws Exception {
		_t = null;
		super.tearDown();
	}

	public void testProperties() {
		checkProperty("YTDHours", new Double(100.0));
		checkProperty("YTDLegs", new Integer(52));
		checkProperty("YTDMiles", new Integer(5311));
		checkProperty("MTDHours", new Double(101.0));
		checkProperty("MTDLegs", new Integer(53));
		checkProperty("MTDMiles", new Integer(5312));
		checkProperty("OnlineMiles", new Long(131250));
		checkProperty("OnlineHours", new Double(31100.0));
		checkProperty("OnlineLegs", new Integer(1552));
		checkProperty("ACARSHours", new Double(105.0));
		checkProperty("ACARSLegs", new Integer(1253));
		checkProperty("ACARSMiles", new Integer(255312));
		checkProperty("TotalMiles", new Long(231250));
		checkProperty("TotalHours", new Double(41100.0));
		checkProperty("TotalLegs", new Integer(2552));
		checkProperty("TotalPilots", new Integer(2510));
		checkProperty("ActivePilots", new Integer(714));
		checkProperty("DBRows", new Integer(163123));
		checkProperty("DBSize", new Long(12163123));
	}
	
	public void testValidation() {
		validateInput("YTDHours", new Double(-1), IllegalArgumentException.class);
		validateInput("YTDLegs", new Integer(-1), IllegalArgumentException.class);
		validateInput("MTDHours", new Double(-1), IllegalArgumentException.class);
		validateInput("MTDLegs", new Integer(-1), IllegalArgumentException.class);
		validateInput("OnlineMiles", new Long(-1), IllegalArgumentException.class);
		validateInput("OnlineHours", new Double(-1), IllegalArgumentException.class);
		validateInput("OnlineLegs", new Integer(-1), IllegalArgumentException.class);
		validateInput("ACARSMiles", new Integer(-1), IllegalArgumentException.class);
		validateInput("ACARSHours", new Double(-1), IllegalArgumentException.class);
		validateInput("ACARSLegs", new Integer(-1), IllegalArgumentException.class);
		validateInput("TotalMiles", new Long(-1), IllegalArgumentException.class);
		validateInput("TotalHours", new Double(-1), IllegalArgumentException.class);
		validateInput("TotalLegs", new Integer(-1), IllegalArgumentException.class);
		validateInput("TotalPilots", new Integer(-1), IllegalArgumentException.class);
		validateInput("ActivePilots", new Integer(-1), IllegalArgumentException.class);
		validateInput("DBSize", new Long(-1), IllegalArgumentException.class);
		validateInput("DBRows", new Integer(-1), IllegalArgumentException.class);
	}
	
	public void testAirlineAge() {
		Calendar c = new GregorianCalendar(2001, 6, 12);
		AirlineTotals at2 = new AirlineTotals(c.getTimeInMillis());
		assertEquals(2, at2.getAge());
		assertEquals(c.getTimeInMillis(), at2.getEffectiveDate());
	}
	
	public void testComparator() {
		AirlineTotals at2 = new AirlineTotals(_t.getEffectiveDate() + 100);
		assertTrue(at2.compareTo(_t) > 0);
		assertTrue(_t.compareTo(at2) < 0);
	}
}