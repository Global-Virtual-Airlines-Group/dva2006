package org.deltava.beans;

import java.util.TimeZone;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestTZInfo extends TestCase {

	private TZInfo _wrapper;

	public static Test suite() {
		return new CoverageDecorator(TestTZInfo.class, new Class[] { TZInfo.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		TZInfo.reset();
	}

	public void testTZGetter() {
		TimeZone est = TimeZone.getTimeZone("US/Eastern");
		_wrapper = TZInfo.init(est.getID(), null, null);
		assertNotNull(_wrapper.getName());
		assertEquals(est, _wrapper.getTimeZone());
		assertEquals(_wrapper.getName(), est.getDisplayName(false, TimeZone.LONG));
		assertEquals("Eastern Standard Time [GMT-5:00/DST]", _wrapper.toString());
	}

	public void testGetAll() {
		assertNotNull(TZInfo.getAll());
		assertEquals(0, TZInfo.getAll().size());
	}
	
	public void testGetNull() {
		assertNull(TZInfo.get(null));
	}

	public void testGMT() {
		_wrapper = TZInfo.UTC;
		assertNotNull(_wrapper.getName());
		assertEquals("Greenwich Mean Time [GMT+0:00]", _wrapper.toString());
		assertEquals(123456, _wrapper.getUTC(123456));
	}

	public void testNameOverride() {
		_wrapper = TZInfo.init("Canada/Atlantic", "Atlantic Standard Time", "AST");
		assertNotNull(_wrapper.getName());
		assertEquals("Atlantic Standard Time", _wrapper.getName());
		assertEquals("Canada/Atlantic", _wrapper.getID());
	}

	public void testZoneGeneration() {
		_wrapper = TZInfo.init("US/Eastern", null, null);
		assertNotNull(_wrapper.getName());
		assertEquals("US/Eastern", _wrapper.getID());
	}

	public void testAbbr() {
		_wrapper = TZInfo.init("US/Pacific", null, null);
		assertNull(_wrapper.getAbbr());
		assertEquals("Pacific Standard Time [GMT-8:00/DST]", _wrapper.toString());
		_wrapper = TZInfo.init("US/Mountain", null, "MST");
		assertNotNull(_wrapper.getAbbr());
		assertEquals("MST", _wrapper.getAbbr());
		assertEquals("Mountain Standard Time (MST) [GMT-7:00/DST]", _wrapper.toString());
	}

	public void testComboAlias() {
		_wrapper = TZInfo.init("US/Eastern", "Eastern Time", "EST");
		assertEquals("US/Eastern", _wrapper.getComboAlias());
		assertEquals(_wrapper.toString(), _wrapper.getComboName());
	}

	public void testLocal() {
		TZInfo.init(TimeZone.getDefault().getID(), null, null);
		TZInfo local = TZInfo.local();
		assertEquals(TimeZone.getDefault().getID(), local.getID());
	}

	public void testComparable() {
		_wrapper = TZInfo.init("US/Eastern", "Eastern Time", "EST");
		TZInfo w2 = TZInfo.init("US/Central", "Central Time", "CT");
		TZInfo w4 = TZInfo.init("Canada/Atlantic", "Atlantic Time", "AST");

		assertEquals(1, _wrapper.compareTo(w2));
		assertEquals(-1, w2.compareTo(_wrapper));
		assertEquals(1, _wrapper.compareTo(null));
		assertEquals(1, w4.compareTo(_wrapper));

		assertFalse(_wrapper.equals(w2));
		assertFalse(_wrapper.equals(new Object()));
		assertFalse(_wrapper.equals(null));
	}
}