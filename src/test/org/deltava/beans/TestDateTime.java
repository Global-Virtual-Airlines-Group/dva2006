package org.deltava.beans;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestDateTime extends TestCase {

	private DateTime _dt;
	private TZInfo _tz;
	
   public static Test suite() {
       return new CoverageDecorator(TestDateTime.class, new Class[] { DateTime.class } );
   }
	
	protected void setUp() throws Exception {
		super.setUp();
		_tz = TZInfo.init(TimeZone.getTimeZone("US/Eastern"));
		assertNotNull(_tz);		
	}
	
	protected void tearDown() throws Exception {
	   TZInfo.reset();
		_tz = null;
		super.tearDown();
	}

	public void testBasics() {
		Date d = new Date();
		_dt = new DateTime(d, _tz);
		assertEquals(_dt.getDate().getTime(), d.getTime());
		assertEquals(_dt.getTimeZone(), _tz);
	}
	
	public void testComparators() {
		long t1 = System.currentTimeMillis();
		long t2 = t1 + 3600000;
		
		_dt = new DateTime(new Date(t1), _tz);
		DateTime dt2 = new DateTime(new Date(t2), _tz);
		DateTime dt3 = new DateTime(new Date(t2), _tz);
		
		assertEquals(-1, _dt.compareTo(dt2));
		assertEquals(1, dt2.compareTo(_dt));
		assertEquals(0, _dt.compareTo(_dt));
		assertFalse(_dt.equals(dt2));
		assertFalse(dt2.equals(_dt));
		assertTrue(_dt.equals(_dt));
		assertTrue(dt2.equals(dt3));
		
		assertEquals(3600, dt2.difference(_dt));
		assertEquals(-3600, _dt.difference(dt2));
		
		assertFalse(_dt.equals(null));
		assertEquals(1, _dt.compareTo(null));
		
		try {
			assertEquals(0, _dt.compareTo(new Object()));
			fail("Exepected ClassCastException");
		} catch (ClassCastException cce) {
			return;
		} catch (Exception e) {
			fail("Exepected ClassCastException");
		}
	}

	public void testDifferentZones() {
		long t1 = System.currentTimeMillis();
		TZInfo cst = TZInfo.init(TimeZone.getTimeZone("US/Central"));
		
		_dt = new DateTime(new Date(t1), _tz);
		DateTime dt2 = new DateTime(new Date(t1), cst);
		
		assertFalse(_dt.equals(dt2));
		assertEquals(-1, _dt.compareTo(dt2));
		assertTrue((_dt.getUTC() != dt2.getUTC()));
		
		assertEquals(3600, dt2.difference(_dt));
	}
	
	public void testZoneConversion() {
		long t1 = System.currentTimeMillis();
		TZInfo cst = TZInfo.init(TimeZone.getTimeZone("US/Central"));
			
		_dt = new DateTime(new Date(t1), _tz);
		DateTime dt2 = new DateTime(new Date(t1), cst);
		DateTime dt3 = new DateTime(new Date(t1 - 3600000), cst);

		assertFalse(_dt.equals(dt2));
		assertFalse(dt2.equals(dt3));
		assertEquals(_dt.getDate().getTime(), dt2.getDate().getTime());
		assertTrue(_dt.equals(dt3));
		assertFalse((dt2.getDate().getTime() == dt3.getDate().getTime()));
		
		dt3.convertTo(_tz);
		assertTrue(dt3.equals(_dt));
		assertEquals(dt3.getDate().getTime(), _dt.getDate().getTime());
	}
	
	public void testToString() throws Exception {
	    TZInfo mdt = TZInfo.init("US/Mountain", "Mountain Time", "MDT");
	    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	    DateTime dt = new DateTime(df.parse("01/20/2005 12:35"), mdt);
	    DateTime dt2 = new DateTime(df.parse("01/20/2005 12:35"), _tz);
	    assertEquals("Thursday January 20 2005 12:35:00 MDT", dt.toString());
	    assertEquals("Thursday January 20 2005 12:35:00", dt2.toString());
	    dt.showZone(false);
	    dt.setDateFormat("MM/dd/yyyy hh:mm");
	    assertEquals("01/20/2005 12:35", dt.toString());
	}
}