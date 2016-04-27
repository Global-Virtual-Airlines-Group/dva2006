package org.deltava.beans.acars;

import java.time.Instant;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airport;

public class TestFlightInfo extends AbstractBeanTestCase {

   private FlightInfo _info;
   
   public static Test suite() {
      return new CoverageDecorator(TestFlightInfo.class, new Class[] { FlightInfo.class } );
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _info = new FlightInfo(1);
      setBean(_info);
   }

   @Override
protected void tearDown() throws Exception {
      _info = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(1, _info.getID());
      checkProperty("pilotID", Integer.valueOf(1234));
      checkProperty("startTime", Instant.now());
      checkProperty("endTime", Instant.now());
      _info.setEndTime(null);
      assertNull(_info.getEndTime());
      checkProperty("flightCode", "DAL043");
      checkProperty("altitude", "FL410");
      checkProperty("equipmentType", "B727-200");
      checkProperty("offline", Boolean.TRUE);
      checkProperty("remarks", "Test remarks");
      checkProperty("FSVersion", Integer.valueOf(2004));
      
      Airport a = new Airport("ATL", "KATL", "Atlanta GA");
      checkProperty("airportA", a);
      checkProperty("airportD", a);
   }
   
   public void testRouteFormatting() {
      _info.setRoute("JFK..LYH..ATL");
      assertEquals("JFK LYH ATL", _info.getRoute());
      _info.setRoute("ATL.LYH.JFK");
      assertEquals("ATL LYH JFK", _info.getRoute());
   }
   
   public void testValidation() {
      validateInput("connectionID", new Long(-1), IllegalArgumentException.class);
      validateInput("pilotID", Integer.valueOf(-1), IllegalArgumentException.class);
      validateInput("FSVersion", Integer.valueOf(-1), IllegalArgumentException.class);
      validateInput("FSVersion", Integer.valueOf(2010), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      _info.setStartTime(Instant.now());
      
      FlightInfo i2 = new FlightInfo(2);
      i2.setStartTime(Instant.now().minusMillis(2));
      assertTrue(_info.getStartTime().isBefore(i2.getStartTime()));
      assertTrue(_info.compareTo(i2) < 0);
      assertTrue(i2.compareTo(_info) > 0);
   }
}