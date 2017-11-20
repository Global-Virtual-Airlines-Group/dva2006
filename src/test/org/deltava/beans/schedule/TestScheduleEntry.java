package org.deltava.beans.schedule;

import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;

public class TestScheduleEntry extends AbstractBeanTestCase {

	private DateTimeFormatter df = null;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb.parseDefaulting(ChronoField.YEAR, now.get(ChronoField.YEAR));
		dfb.parseDefaulting(ChronoField.DAY_OF_YEAR, now.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();

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
		_atl.setTZ(TZInfo.get("US/Eastern"));
		_bhm = new Airport("BHM", "KBHM", "Birmingham AL");
		_bhm.setLocation(33.5629, -86.7535);
		_bhm.setTZ(TZInfo.get("US/Central"));
		_jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
		_jfk.setLocation(40.6397, -73.7789);
		_jfk.setTZ(TZInfo.get("US/Eastern"));
		_nrt = new Airport("NRT", "RJAA", "Tokyo Japan");
		_nrt.setLocation(35.7647, 140.386);
		_nrt.setTZ(TZInfo.get("Asia/Tokyo"));
		_cdg = new Airport("CDG", "LFPG", "Paris-De Gaulle France");
		_cdg.setLocation(49.0128, 2.55);
		_cdg.setTZ(TZInfo.get("Europe/Paris"));
		_eze = new Airport("EZE", "SAEZ", "Buenos Aires Argentina");
		_eze.setLocation(-34.8228, -58.5281);
		_eze.setTZ(TZInfo.get("America/Sao_Paulo"));
		_icn = new Airport("ICN", "RKSI", "Seoul-Inchon South Korea");
		_icn.setLocation(37.4689, 126.45);
		_icn.setTZ(TZInfo.get("Asia/Seoul"));
		_phx = new Airport("PHX", "KHPX", "Phoenix AZ");
		_phx.setLocation(33.4342, -112.008);
		_phx.setTZ(TZInfo.get("US/Arizona"));
		_dfw = new Airport("DFW", "KDFW", "Dallas-Fort Worth TX");
		_dfw.setLocation(32.8956, -97.0367);
		_dfw.setTZ(TZInfo.get("US/Central"));
		_kin = new Airport("KIN", "MKJP", "Kingston Jamaica");
		_kin.setLocation(17.9356, -76.7869);
		_kin.setTZ(TZInfo.get("Jamaica"));

		_e = new ScheduleEntry(_dva, 129, 1);
		setBean(_e);
	}

	@Override
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
		checkProperty("length", Integer.valueOf(35));
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
		validateInput("leg", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("leg", Integer.valueOf(9), IllegalArgumentException.class);
		validateInput("ID", Integer.valueOf(1), UnsupportedOperationException.class);
	}

	public void testNullAirportLength() {
		_e.setTimeD(LocalDateTime.parse("10:05", df));
		_e.setTimeA(LocalDateTime.parse("12:05", df));
		assertEquals(ZoneOffset.UTC, _e.getTimeD().getZone());
		assertEquals(ZoneOffset.UTC, _e.getTimeA().getZone());
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

	public void testLength() {
		_e.setAirportD(_atl);
		_e.setAirportA(_jfk);
		try {
			assertEquals(0, _e.getLength());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setTimeA(LocalDateTime.parse("12:05", df));
		assertNotNull(_e.getTimeA());
		try {
			assertEquals(0, _e.getLength());
			fail("IllegalStateException expected");
		} catch (IllegalStateException ise) {
			// empty
		}

		_e.setTimeD(LocalDateTime.parse("10:15", df));
		assertNotNull(_e.getTimeD());
		assertEquals(18, _e.getLength());
	}

	public void testLongFlightLength() {
		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		assertFalse(_atl.getTZ().getZone().getRules().isDaylightSavings(Instant.now()));
		assertFalse(_nrt.getTZ().getZone().getRules().isDaylightSavings(Instant.now()));
		_e.setTimeD(LocalDateTime.parse("10:25", df));
		_e.setTimeA(LocalDateTime.parse("13:25", df));
		assertEquals(130, _e.getLength());
	}

	public void testSSTLength() {
		_e.setAirportD(_cdg);
		_e.setAirportA(_jfk);
		_e.setEquipmentType("Concorde");
		_e.setTimeD(LocalDateTime.parse("17:00", df));
		_e.setTimeA(LocalDateTime.parse("14:45", df));
		assertEquals(37, _e.getLength());
	}

	public void testNegativeTime() {
		_e.setAirportD(_atl);
		_e.setAirportA(_bhm);
		_e.setEquipmentType("MD-88");
		_e.setTimeD(LocalDateTime.parse("16:50", df));
		_e.setTimeA(LocalDateTime.parse("16:44", df));
		assertEquals(9, _e.getLength());

		// Trans-Atlantic
		_e.setAirportA(_cdg);
		_e.setEquipmentType("B767-300");
		_e.setTimeD(LocalDateTime.parse("17:40", df));
		_e.setTimeA(LocalDateTime.parse("08:15", df));
		assertEquals(85, _e.getLength());
	}

	public void testEuropeSouthAmerica() {
		_e.setAirportD(_cdg);
		_e.setAirportA(_eze);
		_e.setTimeD(LocalDateTime.parse("23:15", df));
		_e.setTimeA(LocalDateTime.parse("07:50", df));
		assertEquals(115, _e.getLength());

		_e.setAirportD(_eze);
		_e.setAirportA(_cdg);
		_e.setTimeD(LocalDateTime.parse("17:20", df));
		_e.setTimeA(LocalDateTime.parse("11:15", df));
		assertEquals(149, _e.getLength());
	}

	public void testCrossIDL() {
		_e.setAirportD(_nrt);
		_e.setAirportA(_atl);
		_e.setTimeD(LocalDateTime.parse("15:30", df));
		_e.setTimeA(LocalDateTime.parse("15:05", df));
		assertEquals(135, _e.getLength());

		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		_e.setTimeD(LocalDateTime.parse("10:25", df));
		_e.setTimeA(LocalDateTime.parse("13:25", df));
		assertEquals(130, _e.getLength());
	}
	
	public void testNoDST() {
		_e.setAirportD(_phx);
		_e.setAirportA(_dfw);
		_e.setTimeD(LocalDateTime.parse("12:30", df));
		_e.setTimeA(LocalDateTime.parse("15:26", df));
		assertFalse(_phx.getTZ().getZone().getRules().isDaylightSavings(Instant.now()));
		boolean dfwDST = _dfw.getTZ().getZone().getRules().isDaylightSavings(Instant.now());
		int time = dfwDST ? 29 : 19;
		assertEquals(time, _e.getLength());
	}
	
	public void testJamaicaDST() {
		_e.setAirportD(_jfk);	
		_e.setAirportA(_kin);
		_e.setTimeD(LocalDateTime.parse("14:00", df));
		_e.setTimeA(LocalDateTime.parse("16:50", df));
		boolean jfkDST = _jfk.getTZ().getZone().getRules().isDaylightSavings(Instant.now());
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