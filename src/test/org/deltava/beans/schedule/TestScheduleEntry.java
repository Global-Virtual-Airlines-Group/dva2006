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
	private Airport _mex;

	public static Test suite() {
		return new CoverageDecorator(TestScheduleEntry.class, new Class[] { ScheduleEntry.class, Flight.class });
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		LocalDate ld = LocalDate.of(2021, 7, 25);
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb.parseDefaulting(ChronoField.YEAR, ld.get(ChronoField.YEAR));
		dfb.parseDefaulting(ChronoField.DAY_OF_YEAR, ld.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();

		TZInfo.init("US/Eastern", null, null);
		TZInfo.init("US/Central", null, null);
		TZInfo.init("Asia/Tokyo", null, null);
		TZInfo.init("Europe/Paris", null, null);
		TZInfo.init("Asia/Seoul", null, null);
		TZInfo.init("America/Sao_Paulo", null, null);
		TZInfo.init("US/Arizona", null, null);
		TZInfo.init("Jamaica", null, null);
		TZInfo.init("America/Mexico_City", null, null);

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
		_mex = new Airport("MEX", "MMMX", "Mexico City Mexico");
		_mex.setLocation(19.435556, -99.070556);
		_mex.setTZ(TZInfo.get("America/Mexico_City"));

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
		assertEquals(-1, _e.getDistance());
		_e.setAirportA(_jfk);
		assertEquals(-1, _e.getDistance());
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
		assertEquals(0, _e.getArrivalPlusDays());
	}

	public void testLongFlightLength() {
		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		_e.setTimeD(LocalDateTime.parse("10:25", df));
		_e.setTimeA(LocalDateTime.parse("13:25", df));
		assertTrue(_atl.getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertFalse(_nrt.getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(140, _e.getLength());
		assertEquals(1, _e.getArrivalPlusDays());
	}

	public void testSSTLength() {
		_e.setAirportD(_cdg);
		_e.setAirportA(_jfk);
		_e.setEquipmentType("Concorde");
		_e.setTimeD(LocalDateTime.parse("17:00", df));
		_e.setTimeA(LocalDateTime.parse("14:45", df));
		assertEquals(37, _e.getLength());
		assertEquals(0, _e.getArrivalPlusDays());
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
		assertEquals(1, _e.getArrivalPlusDays());
	}

	public void testEuropeSouthAmerica() {
		_e.setAirportD(_cdg);
		_e.setAirportA(_eze);
		_e.setTimeD(LocalDateTime.parse("23:15", df));
		_e.setTimeA(LocalDateTime.parse("07:50", df));
		assertTrue(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(135, _e.getLength());
		assertEquals(1, _e.getArrivalPlusDays());

		_e.setAirportD(_eze);
		_e.setAirportA(_cdg);
		_e.setTimeD(LocalDateTime.parse("17:20", df));
		_e.setTimeA(LocalDateTime.parse("11:15", df));
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertTrue(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(129, _e.getLength());
		assertEquals(1, _e.getArrivalPlusDays());
	}

	public void testCrossIDL() {
		_e.setAirportD(_nrt);
		_e.setAirportA(_atl);
		_e.setTimeD(LocalDateTime.parse("15:30", df));
		_e.setTimeA(LocalDateTime.parse("15:05", df));
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertTrue(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(125, _e.getLength());

		_e.setAirportD(_atl);
		_e.setAirportA(_nrt);
		_e.setTimeD(LocalDateTime.parse("10:25", df));
		_e.setTimeA(LocalDateTime.parse("13:25", df));
		assertEquals(140, _e.getLength());
		assertEquals(1, _e.getArrivalPlusDays());
	}
	
	public void testNoDST() {
		_e.setAirportD(_phx);
		_e.setAirportA(_dfw);
		_e.setTimeD(LocalDateTime.parse("12:30", df));
		_e.setTimeA(LocalDateTime.parse("15:26", df));
		assertFalse(_phx.getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		boolean dfwDST = _dfw.getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant());
		int time = dfwDST ? 9 : 19;
		assertEquals(time, _e.getLength());
	}
	
	public void testJamaicaDST() {
		_e.setAirportD(_jfk);	
		_e.setAirportA(_kin);
		_e.setTimeD(LocalDateTime.parse("14:00", df));
		_e.setTimeA(LocalDateTime.parse("16:50", df));
		boolean jfkDST = _jfk.getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant());
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
	
	public void testIntoDSTAdjustment() {
		LocalDate ld = LocalDate.of(2020, 1, 25);
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb = dfb.parseDefaulting(ChronoField.YEAR, ld.get(ChronoField.YEAR)).parseDefaulting(ChronoField.DAY_OF_YEAR, ld.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();
		
		_e.setAirportD(_phx);
		_e.setAirportA(_dfw);
		_e.setTimeD(LocalDateTime.parse("12:30", df));
		_e.setTimeA(LocalDateTime.parse("15:26", df));
		
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(19, _e.getLength());

		LocalDate ld2 = LocalDate.of(2021, 7, 25);
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeD().toLocalTime(), _e.getAirportD().getTZ().getZone()).toInstant()));
		assertTrue(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeA().toLocalTime(), _e.getAirportA().getTZ().getZone()).toInstant()));
		
		assertTrue(_e.adjustForDST(ld2));
		assertEquals(19, _e.getLength());
	}
	
	public void testOutOfDSTAdjustment() {
		LocalDate ld = LocalDate.of(2020, 7, 25);
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb = dfb.parseDefaulting(ChronoField.YEAR, ld.get(ChronoField.YEAR)).parseDefaulting(ChronoField.DAY_OF_YEAR, ld.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();
		
		_e.setAirportD(_dfw);
		_e.setAirportA(_phx);
		
		_e.setTimeD(LocalDateTime.parse("12:30", df));
		_e.setTimeA(LocalDateTime.parse("12:26", df));
		
		assertTrue(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(19, _e.getLength());
		
		LocalDate ld2 = LocalDate.of(2021, 1, 25);
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeD().toLocalTime(), _e.getAirportD().getTZ().getZone()).toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeA().toLocalTime(), _e.getAirportA().getTZ().getZone()).toInstant()));
		
		assertTrue(_e.adjustForDST(ld2));
		assertEquals(19, _e.getLength());
	}
	
	public void testNoDSTAdjustment() {
		LocalDate ld = LocalDate.of(2020, 7, 25);
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb = dfb.parseDefaulting(ChronoField.YEAR, ld.get(ChronoField.YEAR)).parseDefaulting(ChronoField.DAY_OF_YEAR, ld.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();
		
		_e.setAirportD(_atl);
		_e.setAirportA(_jfk);
		
		_e.setTimeD(LocalDateTime.parse("12:30", df));
		_e.setTimeA(LocalDateTime.parse("14:46", df));
		
		assertTrue(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertTrue(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		assertEquals(22, _e.getLength());
		
		LocalDate ld2 = LocalDate.of(2021, 1, 25);
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeD().toLocalTime(), _e.getAirportD().getTZ().getZone()).toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(ZonedDateTime.of(ld2, _e.getTimeA().toLocalTime(), _e.getAirportA().getTZ().getZone()).toInstant()));
		
		assertFalse(_e.adjustForDST(ld2));
		assertEquals(22, _e.getLength());
		assertEquals(0, _e.getArrivalPlusDays());
	}
	
	public void testIDTAdjustment() {
		LocalDate ld = LocalDate.of(2020, 3, 19);
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		dfb = dfb.parseDefaulting(ChronoField.YEAR, ld.get(ChronoField.YEAR)).parseDefaulting(ChronoField.DAY_OF_YEAR, ld.get(ChronoField.DAY_OF_YEAR));
		df = dfb.parseLenient().toFormatter();
		
		_e.setAirportD(_mex);
		_e.setAirportA(_icn);
		
		_e.setTimeD(LocalDateTime.parse("23:10", df));
		_e.setTimeA(LocalDateTime.parse("06:00", df).plusDays(2)); // this should be date+2d
		
		assertFalse(_e.getAirportD().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeD().toInstant()));
		assertFalse(_e.getAirportA().getTZ().getZone().getRules().isDaylightSavings(_e.getTimeA().toInstant()));
		
		assertEquals(158, _e.getLength());
		assertEquals(2, _e.getArrivalPlusDays());
	}
	
	public void testParseDate() {
		df = new DateTimeFormatterBuilder().appendPattern("MM/dd[/yyyy]").parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()).toFormatter();
		assertNotNull(df);
		
		LocalDate ld = LocalDate.parse("07/15/2020", df);
		assertNotNull(ld);
		assertEquals(7, ld.get(ChronoField.MONTH_OF_YEAR));
		assertEquals(15, ld.get(ChronoField.DAY_OF_MONTH));
		assertEquals(2020, ld.get(ChronoField.YEAR));
		
		LocalDate ld2 = LocalDate.parse("08/16/2026", df);
		assertNotNull(ld2);
		assertEquals(8, ld2.get(ChronoField.MONTH_OF_YEAR));
		assertEquals(16, ld2.get(ChronoField.DAY_OF_MONTH));
		assertEquals(2026, ld2.get(ChronoField.YEAR));
	}
}