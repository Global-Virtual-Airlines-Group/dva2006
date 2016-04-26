// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestVOR extends AbstractBeanTestCase {

   private VOR _vor;
   
   public static Test suite() {
      return new CoverageDecorator(TestVOR.class, new Class[] { VOR.class, NavigationDataBean.class });
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _vor = new VOR(12.345, -23.456);
      setBean(_vor);
   }

   @Override
protected void tearDown() throws Exception {
      _vor = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _vor.getLatitude(), 0.0001);
      assertEquals(-23.456, _vor.getLongitude(), 0.0001);
      assertEquals(Navaid.VOR, _vor.getType());
      
      assertNotNull(_vor.getPosition());
      assertEquals(_vor.getLatitude(), _vor.getPosition().getLatitude(), 0.0001);
      assertEquals(_vor.getLongitude(), _vor.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
      checkProperty("name", "Test VOR");
      checkProperty("frequency", "123.35");
      
      assertEquals(_vor.getCode().hashCode(), _vor.hashCode());
      assertEquals(_vor.getCode(), _vor.cacheKey());
   }
   
   public void testValidation() {
      validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
      validateInput("type", Integer.valueOf(11), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      _vor.setCode("TST");
      
      VOR vor2 = new VOR(12.346, -25.1632);
      vor2.setCode("ABC");
      
      assertTrue(_vor.compareTo(vor2) > 0);
      assertTrue(vor2.compareTo(_vor) < 0);
      assertFalse(_vor.equals(vor2));
      
      vor2.setCode("TST");
      assertTrue(_vor.equals(vor2));
      assertFalse(_vor.equals(new Object()));
      assertFalse(_vor.equals(null));
   }
}