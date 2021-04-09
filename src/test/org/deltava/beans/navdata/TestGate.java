// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airline;

public class TestGate extends AbstractBeanTestCase {

   private Gate _g;
   
   public static Test suite() {
      return new CoverageDecorator(TestGate.class, new Class[] { NDB.class, Gate.class });
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _g = new Gate(12.345, -23.456);
      setBean(_g);
   }

   @Override
protected void tearDown() throws Exception {
      _g = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _g.getLatitude(), 0.0001);
      assertEquals(-23.456, _g.getLongitude(), 0.0001);
      assertEquals(Navaid.GATE, _g.getType());
      assertEquals(GateZone.DOMESTIC, _g.getZone());
      
      assertNotNull(_g.getPosition());
      assertEquals(_g.getLatitude(), _g.getPosition().getLatitude(), 0.0001);
      assertEquals(_g.getLongitude(), _g.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
      checkProperty("region", "K7");
      checkProperty("name", "TEST VOR");
      
      assertEquals(_g.getCode(), _g.cacheKey());
   }
   
   public void testComparator() {
      _g.setCode("TST");
      
      Gate g2 = new Gate(12.346, -25.1632);
      g2.setCode("ABC");
      
      assertTrue(_g.compareTo(g2) > 0);
      assertTrue(g2.compareTo(_g) < 0);
      assertFalse(_g.equals(g2));
      
      g2.setCode("TST");
      assertTrue(_g.equals(g2));
      assertFalse(_g.equals(new Object()));
      assertFalse(_g.equals(null));
   }
   
   public void testClone() throws Exception {
	   _g.setHeading(123);
	   _g.setZone(GateZone.INTERNATIONAL);
	   _g.setUseCount(11);
	   _g.setCode("KATL");
	   _g.setName("GATE 15");
	   _g.addAirline(new Airline("DL"));
	   
	   Gate g2 = (Gate) _g.clone();
	   assertNotNull(g2);
	   assertNotSame(_g, g2);
	   assertEquals(_g.getCode(), g2.getCode());
	   assertEquals(_g.getName(), g2.getName());
	   assertEquals(_g.getLatitude(), g2.getLatitude(), 0.0001);
	   assertEquals(_g.getLongitude(), g2.getLongitude(), 0.0001);
	   assertEquals(_g.getZone(), g2.getZone());
	   assertEquals(_g.getHeading(), g2.getHeading());
	   assertEquals(_g.getGateType(), g2.getGateType());
	   assertEquals(_g.getAirlines().size(), g2.getAirlines().size());
   }
}