// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestAirportLocation extends AbstractBeanTestCase {

   private AirportLocation _a;
   
   public static Test suite() {
      return new CoverageDecorator(TestAirportLocation.class, new Class[] { AirportLocation.class, NavigationDataBean.class });
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _a = new AirportLocation(12.345, -23.456);
      setBean(_a);
   }

   protected void tearDown() throws Exception {
      _a = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _a.getLatitude(), 0.0001);
      assertEquals(-23.456, _a.getLongitude(), 0.0001);
      assertEquals(NavigationDataBean.AIRPORT, _a.getType());
      assertEquals(NavigationDataBean.NAVTYPE_NAMES[NavigationDataBean.AIRPORT], _a.getTypeName());
      
      assertNotNull(_a.getPosition());
      assertEquals(_a.getLatitude(), _a.getPosition().getLatitude(), 0.0001);
      assertEquals(_a.getLongitude(), _a.getPosition().getLongitude(), 0.0001);
      
      checkProperty("altitude", new Integer(580));
      checkProperty("code", "TST");
      checkProperty("name", "Test Airport");
      
      assertEquals(_a.getCode().hashCode(), _a.hashCode());
      assertEquals(_a.getCode(), _a.cacheKey());
   }
   
   public void testValidation() {
      validateInput("type", new Integer(-1), IllegalArgumentException.class);
      validateInput("type", new Integer(11), IllegalArgumentException.class);
      validateInput("altitude", new Integer(-301), IllegalArgumentException.class);
      validateInput("altitude", new Integer(29001), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      _a.setCode("TST");
      
      AirportLocation a2 = new AirportLocation(12.346, -25.1632);
      a2.setCode("ABC");
      
      assertTrue(_a.compareTo(a2) > 0);
      assertTrue(a2.compareTo(_a) < 0);
      assertFalse(_a.equals(a2));
      
      a2.setCode("TST");
      assertTrue(_a.equals(a2));
      assertFalse(_a.equals(new Object()));
      assertFalse(_a.equals(null));
   }
}