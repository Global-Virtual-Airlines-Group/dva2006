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
   
   protected void setUp() throws Exception {
      super.setUp();
      _rwy = new Runway(12.345, -23.456);
      setBean(_rwy);
   }

   protected void tearDown() throws Exception {
      _rwy = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _rwy.getLatitude(), 0.0001);
      assertEquals(-23.456, _rwy.getLongitude(), 0.0001);
      assertEquals(NavigationDataBean.RUNWAY, _rwy.getType());
      assertEquals(NavigationDataBean.NAVTYPE_NAMES[NavigationDataBean.RUNWAY], _rwy.getTypeName());
      
      assertNotNull(_rwy.getPosition());
      assertEquals(_rwy.getLatitude(), _rwy.getPosition().getLatitude(), 0.0001);
      assertEquals(_rwy.getLongitude(), _rwy.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
      checkProperty("heading", new Integer(92));
      checkProperty("length", new Integer(8050));
      checkProperty("name", "Test VOR");
      checkProperty("frequency", "123.35");
      
      assertEquals(_rwy.getCode().hashCode(), _rwy.hashCode());
      assertEquals(_rwy.getCode(), _rwy.cacheKey());
   }
   
   public void testValidation() {
      validateInput("type", new Integer(-1), IllegalArgumentException.class);
      validateInput("type", new Integer(11), IllegalArgumentException.class);
      validateInput("length", new Integer(0), IllegalArgumentException.class);
      validateInput("length", new Integer(25001), IllegalArgumentException.class);
      validateInput("heading", new Integer(-1), IllegalArgumentException.class);
      validateInput("heading", new Integer(361), IllegalArgumentException.class);
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