// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

@SuppressWarnings("static-method")
public class TestIntersection extends AbstractBeanTestCase {

   private Intersection _int;
   
   public static Test suite() {
      return new CoverageDecorator(TestIntersection.class, new Class[] { Intersection.class });
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _int = new Intersection("ABC", 12.345, -23.456);
      setBean(_int);
   }

   @Override
protected void tearDown() throws Exception {
      _int = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _int.getLatitude(), 0.0001);
      assertEquals(-23.456, _int.getLongitude(), 0.0001);
      assertEquals(Navaid.INT, _int.getType());
      
      assertNotNull(_int.getPosition());
      assertEquals(_int.getLatitude(), _int.getPosition().getLatitude(), 0.0001);
      assertEquals(_int.getLongitude(), _int.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
   }
   
   public void testValidation() {
      validateInput("name", "TEST", UnsupportedOperationException.class);
      try {
         _int.getName();
         fail("UnsupportedOperationException expected");
      } catch (UnsupportedOperationException uoe) {
    	// empty
      }
   }
   
   public void testCoordinates() {
	   assertEquals(CodeType.QUADRANT, NavigationDataBean.isCoordinates("5052N"));
	   assertEquals(CodeType.QUADRANT, NavigationDataBean.isCoordinates("5052S"));
	   assertEquals(CodeType.QUADRANT, NavigationDataBean.isCoordinates("5052E"));
	   assertEquals(CodeType.QUADRANT, NavigationDataBean.isCoordinates("5052W"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("50N52W"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("50N52E"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("50S52W"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("50S52E"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("48N030W"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("FOO"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("VASA1"));
   }
   
   public void testParse() {
	   Intersection i = Intersection.parse("5250N");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(52.0, i.getLatitude(), 0.001);
	   assertEquals(-50.0, i.getLongitude(), 0.001);
	   
	   Intersection i2 = Intersection.parse("52/50");
	   assertNotNull(i2);
	   assertEquals(Navaid.INT, i2.getType());
	   assertEquals(52.0, i2.getLatitude(), 0.001);
	   assertEquals(-50.0, i2.getLongitude(), 0.001);

	   i = Intersection.parse("5250S");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(-52.0, i.getLatitude(), 0.001);
	   assertEquals(50.0, i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("35160E");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(35.0, i.getLatitude(), 0.001);
	   assertEquals(160.0, i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("35N160E");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(35.0, i.getLatitude(), 0.001);
	   assertEquals(160.0, i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("48N030W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(48.0, i.getLatitude(), 0.001);
	   assertEquals(-30.0, i.getLongitude(), 0.001);
   }
}