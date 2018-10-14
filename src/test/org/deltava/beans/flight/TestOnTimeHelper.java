package org.deltava.beans.flight;

import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.util.Collections;

import org.deltava.beans.TZInfo;
import org.deltava.beans.schedule.*;

import junit.framework.TestCase;

public class TestOnTimeHelper extends TestCase {
	
	private DateTimeFormatter df;
	
	private final Airline _dva = new Airline("DVA", "Delta Virtual Airlines");
	private final Airline _klm = new Airline("KLM", "KLM Royal Dutch Airlines");
	
	private Airport _lax;
	private Airport _ams;
	private Airport _mco;
	private Airport _mia;
	
	private static class MockFlightTimes implements FlightTimes {

		private final ZonedDateTime _tD;
		private final ZonedDateTime _tA;
		
		MockFlightTimes(ZonedDateTime timeD, ZonedDateTime timeA) {
			_tD = timeD;
			_tA = timeA;
		}
		
		@Override
		public ZonedDateTime getTimeD() {
			return _tD;
		}

		@Override
		public ZonedDateTime getTimeA() {
			return _tA;
		}
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
		TZInfo.init("US/Pacific", null, null);
		TZInfo.init("Europe/Paris", null, null);
		
		_lax = new Airport("LAX", "KLAX", "Los Angeles CA");
		_lax.setLocation(33.9425, -118.408);
		_lax.setTZ(TZInfo.get("US/Pacific"));
		
		_ams = new Airport("AMS", "EHAM", "Amsterdam Netherlands");
		_ams.setLocation(52.3081, 4.76278);
		_ams.setTZ(TZInfo.get("Europe/Paris"));
		
		_mco = new Airport("MCO", "KMCO", "Orlando FL");
		_mco.setLocation(28.42888, -81.315552);
		_mco.setTZ(TZInfo.get("US/Eastern"));
		
		_mia = new Airport("MIA", "KMIA", "Miami FL");
		_mia.setLocation(25.793056, -80.290558);
		_mia.setTZ(TZInfo.get("US/Eastern"));
	}

	public void testMatchEntry() {
		
		// Build the schedule entry
		ScheduleEntry se = new ScheduleEntry(_klm, 602, 1);
		se.setAirportD(_lax);
		se.setAirportA(_ams);
		se.setTimeD(LocalDateTime.parse("13:50", df));
		se.setTimeA(LocalDateTime.parse("09:05", df));
		
		// Build the mock flight report
		ZonedDateTime timeD = ZonedDateTime.of(LocalDateTime.parse("13:56", df), _lax.getTZ().getZone());
		ZonedDateTime timeA = ZonedDateTime.of(LocalDateTime.parse("09:13", df), _ams.getTZ().getZone()).plusDays(1);
		MockFlightTimes fr = new MockFlightTimes(timeD, timeA);
		
		// Check
		OnTimeHelper oth = new OnTimeHelper(Collections.singleton(se));
		OnTime ot = oth.validate(fr);
		assertNotNull(ot);
		assertEquals(OnTime.LATE, ot);
		assertNotNull(oth.getScheduleEntry());
		assertSame(oth.getScheduleEntry(), se);
	}
	
	public void testOutOfRangeEntry() {

		// Build the schedule entry
		ScheduleEntry se = new ScheduleEntry(_dva, 5788, 1);
		se.setAirportD(_mco);
		se.setAirportA(_mia);
		se.setTimeD(LocalDateTime.parse("11:45", df));
		se.setTimeA(LocalDateTime.parse("12:45", df));
		
		// Build the mock flight report
		ZonedDateTime timeD = ZonedDateTime.of(LocalDateTime.parse("18:02", df), _mco.getTZ().getZone());
		ZonedDateTime timeA = ZonedDateTime.of(LocalDateTime.parse("18:44", df), _mia.getTZ().getZone());
		MockFlightTimes fr = new MockFlightTimes(timeD, timeA);
		
		OnTimeHelper oth = new OnTimeHelper(Collections.singleton(se));
		OnTime ot = oth.validate(fr);
		assertNotNull(ot);
		assertEquals(OnTime.UNKNOWN, ot);
		assertNull(oth.getScheduleEntry());
	}
	
	public void testOnTime() {
		
		// Build the schedule entry
		ScheduleEntry se = new ScheduleEntry(_klm, 602, 1);
		se.setAirportD(_lax);
		se.setAirportA(_ams);
		se.setTimeD(LocalDateTime.parse("13:50", df));
		se.setTimeA(LocalDateTime.parse("09:05", df));
		
		// Build the early flight report
		ZonedDateTime timeD = ZonedDateTime.of(LocalDateTime.parse("13:56", df), _lax.getTZ().getZone());
		ZonedDateTime timeA = ZonedDateTime.of(LocalDateTime.parse("08:45", df), _ams.getTZ().getZone()).plusDays(1);
		MockFlightTimes efr = new MockFlightTimes(timeD, timeA);
		
		// Build the ontime flight report
		timeD = ZonedDateTime.of(LocalDateTime.parse("13:56", df), _lax.getTZ().getZone());
		timeA = ZonedDateTime.of(LocalDateTime.parse("09:00", df), _ams.getTZ().getZone()).plusDays(1);
		MockFlightTimes ofr = new MockFlightTimes(timeD, timeA);
		
		// Build the late flight report
		timeD = ZonedDateTime.of(LocalDateTime.parse("13:56", df), _lax.getTZ().getZone());
		timeA = ZonedDateTime.of(LocalDateTime.parse("09:06", df), _ams.getTZ().getZone()).plusDays(1);
		MockFlightTimes lfr = new MockFlightTimes(timeD, timeA);
		
		// Check
		OnTimeHelper oth = new OnTimeHelper(Collections.singleton(se));
		OnTime ot = oth.validate(efr);
		assertNotNull(ot);
		assertEquals(OnTime.EARLY, ot);
		ot = oth.validate(ofr);
		assertNotNull(ot);
		assertEquals(OnTime.ONTIME, ot);
		ot = oth.validate(lfr);
		assertNotNull(ot);
		assertEquals(OnTime.LATE, ot);
	}
}