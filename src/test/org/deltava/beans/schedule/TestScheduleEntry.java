package org.deltava.beans.schedule;

import java.text.*;
import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;

public class TestScheduleEntry extends AbstractBeanTestCase {

	private static final DateFormat df = new SimpleDateFormat("HH:mm");

	private Airline _dva = new Airline("DVA", "Delta Virtual Airlines");
	private Airline _afv = new Airline("AFV", "Aviation Francais Virtuel");

	private ScheduleEntry _e;
	private Airport _atl;
	private Airport _bhm;
	private Airport _jfk;
	private Airport _nrt;
	private Airport _cdg;
	private Airport _eze;
	private Airport _icn;
	private Airport _phx;
	private Airport _dfw;
	private Airport _kin;

	public static Test suite() {
		return new CoverageDecorator(TestScheduleEntry.class, new Class[] { ScheduleEntry.class, Flight.class });
	}

	protected void setUp() throws Exception {
		super.setUp();

		TZInfo.init("US/Eastern", null, null);
		TZInfo.init("US/Central", null, null);
		TZInfo.init("Asia/Tokyo", null, null);
		TZInfo.init("Europe/Paris", null, null);
		TZInfo.init("Asia/Seoul", null, null);
		TZInfo.init("America/Sao_Paulo", null, null);
		TZInfo.init("US/Arizona", null, null);
		TZInfo.init("Jamaica", null, null);

		_atl = new Airport("ATL", "KATL", "Atlanta GA");
		_atl.setLocation(34.6404, -84.4269);
		_atl.setTZ("US/Eastern");
		_bhm = new Airport("BHM", "KBHM", "Birmingham AL");
		_bhm.setLocation(33.5629, -86.7535);
		_bhm.setTZ("US/Central");
		_jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
		_jfk.setLocation(40.6397, -73.7789);
		_jfk.setTZ("US/Eastern");
		_nrt = new Airport("NRT", "RJAA", "Tokyo Japan");
		_nrt.setLocation(35.7647, 140.386);
		_nrt.setTZ("Asia/Tokyo");
		_cdg = new Airport("CDG", "LFPG", "Paris-De Gaulle France");
		_cdg.setLocation(49.0128, 2.55);
		_cdg.setTZ("Europe/Paris");
		_eze = new Airport("EZE", "SAEZ", "Buenos Aires Argentina");
		_eze.setLocation(-34.8228, -58.5281);
		_eze.setTZ("America/Sao_Paulo");
		_icn = new Airport("ICN", "RKSI", "Seoul-Inchon South Korea");
		_icn.setLocation(37.4689, 126.45);
		_icn.setTZ("Asia/Seoul");
		_phx = new Airport("PHX", "KHPX", "Phoenix AZ");
		_phx.setLocation(33.4342, -112.008);
		_icn.setTZ("US/Arizona");
		_dfw = new Airport("DFW", "KDFW", "Dallas-Fort Worth TX");
		_dfw.setLocation(32.8956, -97.0367);
		_dfw.setTZ("US/Central");
		_kin = new Airport("KIN", "MKJP", "Kingston Jamaica");
		_kin.setLocation(17.9356, -76.7869);
		_kin.setTZ("Jamaica");

		_e = new ScheduleEntry(_dva, 129, 1);
		setBean(_e);
	}

	protected void tearDown() throws Exception {
		_e = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals("DVA", _e.getAirline().getCode());
		assertEquals(129, _e.getFlightNumber());
		assertEquals(1, _e.getLeg());
		assertEquals("DVA129 Leg 1", _e.toString());
		checkProperty("equipmentType", "B767-300");
		checkProperty("airportA", new Airport("ATL", "KATL", "Atlanta GA"));
		checkProperty("airportD", new Airport("CLT", "KCLT", "Charlotte NC"));
		checkProperty("length", new Integer(35));
		checkProperty("historic", Boolean.valueOf(true));
		checkProperty("canPurge", Boolean.valueOf(true));
		assertTrue(_e.equals(new ScheduleEntry(_dva, 129, 1)));
		assertFalse(_e.equals(new ScheduleEntry(_dva, 130, 1)));
		assertFalse(_e.equals(new ScheduleEntry(_dva, 129, 2)));
		assertFalse(_e.equals(new ScheduleEntry(_afv, 129, 1)));
	}

	public void testLeg0Constructor() {
		ScheduleEntry e2 = new ScheduleEntry(_dva, 1, 0);
		assertEquals(1, e2.getLeg());
	}

	public void testValidation() {
		validateInput("leg", new Integer(-1), IllegalArgumentException.class);
		validateInput("leg", new Integer(9), IllegalArgumentException.class);
		validateInput("flightNumber", new Integer(-1), IllegalArgumentException.class);
		validateInput("ID", new Integer(1), UnsupportedOperationException.class);
		validateInput("length", new Integer(-1), IllegalArgumentException.class);
	}

	public void testNullAirportLength() throws ParseException {
		_e.setTimeD(df.parse("10:05"));
		_e.setTimeA(df.parse("12:05"));
		assertEquals(TZInfo.local(), _e.getDateTimeD().getTimeZone());
		assertEquals(TZInfo.local(), _e.getDateTimeA().getTimeZone());
	}

	public void testDistance() {
		try {
			assertEquals(0, _e.getDistance());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setAirportA(_jfk);

		try {
			assertEquals(0, _e.getDistance());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setAirportD(_atl);
		assertEquals(715, _e.getDistance());
	}

	public void testLength() throws ParseException {
		_e.setAirportD(_atl);
		_e.setAirportA(_jfk);
		try {
			assertEquals(0, _e.getLength());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setTimeA(df.parse("12:05"));
		assertNotNull(_e.getDateTimeA());
		assertEquals(_e.getDateTimeA().getDate(), _e.getTimeA());
		try {
			assertEquals(0, _e.getLength());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setTimeD(df.parse("10:15"));
		assertNotNull(_e.getDateTimeD());
		assertEquals(_e.getDateTimeD().getDate(), _e.getTimeD());
		assertEquals(18, _e.getLength());
	}

	public void testLongFlightLength() throws ParseException {
		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		assertTrue(_atl.getTZ().getTimeZone().inDaylightTime(new Date()));
		assertTrue(_nrt.getTZ().getTimeZone().inDaylightTime(new Date()));
		_e.setTimeD(df.parse("10:25"));
		_e.setTimeA(df.parse("13:25"));
		assertEquals(130, _e.getLength());
	}

	public void testSSTLength() throws ParseException {
		_e.setAirportD(_cdg);
		_e.setAirportA(_jfk);
		_e.setEquipmentType("Concorde");
		_e.setTimeD(df.parse("17:00"));
		_e.setTimeA(df.parse("14:45"));
		assertEquals(37, _e.getLength());
	}

	public void testNegativeTime() throws ParseException {
		_e.setAirportD(_atl);
		_e.setAirportA(_bhm);
		_e.setEquipmentType("MD-88");
		_e.setTimeD(df.parse("16:50"));
		_e.setTimeA(df.parse("16:44"));
		assertEquals(9, _e.getLength());

		// Trans-Atlantic
		_e.setAirportA(_cdg);
		_e.setEquipmentType("B767-300");
		_e.setTimeD(df.parse("17:40"));
		_e.setTimeA(df.parse("08:15"));
		assertEquals(85, _e.getLength());
	}

	public void testEuropeSouthAmerica() throws ParseException {
		_e.setAirportD(_cdg);
		_e.setAirportA(_eze);
		_e.setTimeD(df.parse("23:15"));
		_e.setTimeA(df.parse("07:50"));
		assertEquals(125, _e.getLength());

		_e.setAirportD(_eze);
		_e.setAirportA(_cdg);
		_e.setTimeD(df.parse("17:20"));
		_e.setTimeA(df.parse("11:15"));
		assertEquals(139, _e.getLength());
	}

	public void testCrossIDL() throws ParseException {
		_e.setAirportD(_nrt);
		_e.setAirportA(_atl);
		_e.setTimeD(df.parse("15:30"));
		_e.setTimeA(df.parse("15:05"));
		assertEquals(135, _e.getLength());

		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		_e.setTimeD(df.parse("10:25"));
		_e.setTimeA(df.parse("13:25"));
		assertEquals(130, _e.getLength());
	}
	
	public void testNoDST() throws ParseException {
		_e.setAirportD(_phx);
		_e.setAirportA(_dfw);
		_e.setTimeD(df.parse("12:30"));
		_e.setTimeA(df.parse("14:26"));
		boolean dfwDST = _dfw.getTZ().getTimeZone().inDaylightTime(new Date());
		int time = dfwDST ? 29 : 19;
		assertEquals(time, _e.getLength());
	}
	
	public void testJamaicaDST() throws ParseException {
		_e.setAirportD(_jfk);	
		_e.setAirportA(_kin);
		_e.setTimeD(df.parse("14:00"));
		_e.setTimeA(df.parse("16:50"));
		boolean jfkDST = _jfk.getTZ().getTimeZone().inDaylightTime(new Date());
		int time = jfkDST ? 38 : 28;
		assertEquals(time, _e.getLength());
	}

	public void testComparator() {
		ScheduleEntry e3 = new ScheduleEntry(_afv, 129, 1);
		ScheduleEntry e4 = new ScheduleEntry(_dva, 129, 2);
		ScheduleEntry e5 = new ScheduleEntry(_dva, 130, 1);
		assertEquals(0, _e.compareTo(_e));
		assertTrue(_e.compareTo(e3) > 0);
		assertTrue(_e.compareTo(e4) < 0);
		assertTrue(_e.compareTo(e5) < 0);
	}

	public void testRowClassName() {
		assertNull(_e.getRowClassName());
		_e.setHistoric(true);
		assertEquals("opt2", _e.getRowClassName());
	}

	public void testID() {
		try {
			assertEquals(0, _e.getID());
			fail("UnsupportedOperationException expected");
		} catch (UnsupportedOperationException uoe) {
			// empty
		}
	}
}