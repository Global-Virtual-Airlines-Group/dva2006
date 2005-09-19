// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airport;

public class TestFlightInfo extends AbstractBeanTestCase {

   private FlightInfo _info;
   private long _conID;
   
   public static Test suite() {
      return new CoverageDecorator(TestFlightInfo.class, new Class[] { FlightInfo.class } );
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _conID = System.currentTimeMillis();
      _info = new FlightInfo(1, _conID);
      setBean(_info);
   }

   protected void tearDown() throws Exception {
      _info = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(1, _info.getID());
      assertEquals(_conID, _info.getConnectionID());
      checkProperty("pilotID", new Integer(1234));
      checkProperty("startTime", new Date());
      checkProperty("endTime", new Date());
      _info.setEndTime(null);
      assertNull(_info.getEndTime());
      checkProperty("flightCode", "DAL043");
      checkProperty("altitude", "FL410");
      checkProperty("equipmentType", "B727-200");
      checkProperty("offline", Boolean.TRUE);
      checkProperty("remarks", "Test remarks");
      checkProperty("FSVersion", new Integer(2004));
      
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
      validateInput("pilotID", new Integer(-1), IllegalArgumentException.class);
      validateInput("FSVersion", new Integer(-1), IllegalArgumentException.class);
      validateInput("FSVersion", new Integer(2010), IllegalArgumentException.class);
      
      long now = System.currentTimeMillis();
      _info.setStartTime(new Date(now));
      _info.setEndTime(new Date(now - 1));
      assertEquals(_info.getStartTime(), _info.getEndTime());
   }
   
   public void testComparator() {
      long now = System.currentTimeMillis();
      _info.setStartTime(new Date(now));
      
      FlightInfo i2 = new FlightInfo(2, _conID);
      i2.setStartTime(new Date(now + 1));
      assertTrue(_info.getStartTime().before(i2.getStartTime()));
      assertTrue(_info.compareTo(i2) < 0);
      assertTrue(i2.compareTo(_info) > 0);
   }
}