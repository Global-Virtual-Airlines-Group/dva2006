// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestRunway extends AbstractBeanTestCase {

   private Runway _rwy;
   
   public static Test suite() {
      return new CoverageDecorator(TestRunway.class, new Class[] { Runway.class, NavigationDataBean.class });
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _rwy = new Runway(12.345, -23.456);
      setBean(_rwy);
   }

   @Override
protected void tearDown() throws Exception {
      _rwy = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _rwy.getLatitude(), 0.0001);
      assertEquals(-23.456, _rwy.getLongitude(), 0.0001);
      assertEquals(Navaid.RUNWAY, _rwy.getType());
      
      checkProperty("code", "TST");
      checkProperty("heading", Integer.valueOf(92));
      checkProperty("length", Integer.valueOf(8050));
      checkProperty("name", "Test VOR");
      checkProperty("frequency", "123.35");
      assertEquals(_rwy.getCode(), _rwy.cacheKey());
   }
   
   public void testComparator() {
      _rwy.setCode("TST");
      
      Runway rwy2 = new Runway(12.346, -25.1632);
      rwy2.setCode("ABC");
      
      assertTrue(_rwy.compareTo(rwy2) > 0);
      assertTrue(rwy2.compareTo(_rwy) < 0);
      assertFalse(_rwy.equals(rwy2));
      
      rwy2.setCode("TST");
      assertTrue(_rwy.equals(rwy2));
      assertFalse(_rwy.equals(new Object()));
      assertFalse(_rwy.equals(null));
   }
}