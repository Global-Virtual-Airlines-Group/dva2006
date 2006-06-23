package org.deltava.beans.schedule;

import java.text.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;

public class TestScheduleEntry extends AbstractBeanTestCase {

   private Airline _dva = new Airline("DVA", "Delta Virtual Airlines");
   private Airline _afv = new Airline("AFV", "Aviation Francais Virtuel");

   private ScheduleEntry _e;
   private Airport _atl;
   private Airport _bhm;
   private Airport _jfk;
   private Airport _nrt;
   private Airport _cdg;

   public static Test suite() {
      return new CoverageDecorator(TestScheduleEntry.class, new Class[] { ScheduleEntry.class, Flight.class });
   }

   protected void setUp() throws Exception {
      super.setUp();

      TZInfo.init("US/Eastern", null, null);
      TZInfo.init("US/Central", null, null);
      TZInfo.init("Asia/Tokyo", null, null);
      TZInfo.init("Europe/Paris", null, null);

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
	   DateFormat df = new SimpleDateFormat("HH:mm");
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
      }

      _e.setAirportA(_jfk);

      try {
         assertEquals(0, _e.getDistance());
         fail("IllegalStateException expected");
      } catch (IllegalStateException ise) {
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
      }

      DateFormat df = new SimpleDateFormat("HH:mm");

      _e.setTimeA(df.parse("12:05"));
      assertNotNull(_e.getDateTimeA());
      assertEquals(_e.getDateTimeA().getDate(), _e.getTimeA());
      try {
         assertEquals(0, _e.getLength());
         fail("IllegalStateException expected");
      } catch (IllegalStateException ise) {
      }

      _e.setTimeD(df.parse("10:15"));
      assertNotNull(_e.getDateTimeD());
      assertEquals(_e.getDateTimeD().getDate(), _e.getTimeD());
      assertEquals(18, _e.getLength());
   }
   
   public void testLongFlightLength() throws ParseException {
	   _e.setAirportD(_atl);
	   _e.setAirportA(_nrt);
	   
	   DateFormat df = new SimpleDateFormat("HH:mm");
	   _e.setTimeD(df.parse("10:25"));
	   _e.setTimeA(df.parse("13:25"));
	   assertEquals(130, _e.getLength());
   }
   
   public void testSSTLength() throws ParseException {
	   _e.setAirportD(_cdg);
	   _e.setAirportA(_jfk);
	   _e.setEquipmentType("Concorde");
	   
	   DateFormat df = new SimpleDateFormat("HH:mm");
	   _e.setTimeD(df.parse("17:00"));
	   _e.setTimeA(df.parse("14:45"));
	   assertEquals(37, _e.getLength());
   }
   
   public void testNegativeTime() throws ParseException {
	   _e.setAirportD(_atl);
	   _e.setAirportA(_bhm);
	   _e.setEquipmentType("MD-88");
	   
	   DateFormat df = new SimpleDateFormat("HH:mm");
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