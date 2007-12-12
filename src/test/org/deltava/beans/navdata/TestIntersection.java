// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestIntersection extends AbstractBeanTestCase {

   private Intersection _int;
   
   public static Test suite() {
      return new CoverageDecorator(TestIntersection.class, new Class[] { Intersection.class });
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _int = new Intersection("ABC", 12.345, -23.456);
      setBean(_int);
   }

   protected void tearDown() throws Exception {
      _int = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _int.getLatitude(), 0.0001);
      assertEquals(-23.456, _int.getLongitude(), 0.0001);
      assertEquals(NavigationDataBean.INT, _int.getType());
      assertEquals(NavigationDataBean.NAVTYPE_NAMES[NavigationDataBean.INT], _int.getTypeName());
      
      assertNotNull(_int.getPosition());
      assertEquals(_int.getLatitude(), _int.getPosition().getLatitude(), 0.0001);
      assertEquals(_int.getLongitude(), _int.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
   }
   
   public void testValidation() {
      validateInput("type", new Integer(-1), IllegalArgumentException.class);
      validateInput("type", new Integer(11), IllegalArgumentException.class);
      validateInput("name", "TEST", UnsupportedOperationException.class);
      try {
         _int.getName();
         fail("UnsupportedOperationException expected");
      } catch (UnsupportedOperationException uoe) {
      }
   }
}