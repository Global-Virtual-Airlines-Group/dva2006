// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.GeoPosition;

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
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("N46W015"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("485823N0302231W"));
	   assertEquals(CodeType.FULL, NavigationDataBean.isCoordinates("5430N02000W"));
	   assertEquals(CodeType.SLASH, NavigationDataBean.isCoordinates("4000N/16000E"));
   }
   
   public void testCode() {
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("FOO"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("VASA1"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("17VOR"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("70TJE"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("EX"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("N640A"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("YIN/K0952S0890"));
	   assertEquals(CodeType.CODE, NavigationDataBean.isCoordinates("SOVAT/N0486F320"));
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
	   
	   i = Intersection.parse("5230/50");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(52.5, i.getLatitude(), 0.001);
	   assertEquals(-50.0, i.getLongitude(), 0.001);

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
	   
	   GeoPosition gp = new GeoPosition();
	   gp.setLatitude(48, 58, 23);
	   gp.setLongitude(-30, 22, 31);
	   
	   i = Intersection.parse("485823N0302231W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   gp = new GeoPosition();
	   gp.setLatitude(-48, 18, 3);
	   gp.setLongitude(-130, 2, 1);
	   
	   i = Intersection.parse("481803S1300201W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   gp = new GeoPosition();
	   gp.setLatitude(40, 15, 15);
	   gp.setLongitude(-95, 10, 20);
	   
	   i = Intersection.parse("401515N/951020W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   gp = new GeoPosition();
	   gp.setLatitude(54, 30, 0);
	   gp.setLongitude(-20, 0, 0);
	   
	   i = Intersection.parse("5430N02000W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   gp = new GeoPosition();
	   gp.setLatitude(57, 30, 0);
	   gp.setLongitude(-100, 0, 0);
	   
	   i = Intersection.parse("5730N1000000W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("5730N10000W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("5730N100W");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   gp = new GeoPosition();
	   gp.setLatitude(46, 0, 0);
	   gp.setLongitude(-15, 0, 0);
	   
	   i = Intersection.parse("N46W015");
	   assertNotNull(i);
	   assertEquals(Navaid.INT, i.getType());
	   assertEquals(gp.getLatitude(), i.getLatitude(), 0.001);
	   assertEquals(gp.getLongitude(), i.getLongitude(), 0.001);
	   
	   i = Intersection.parse("65N20");
	   assertNull(i);
	   
	   i = Intersection.parse("65N20/N0476F370");
	   assertNull(i);
	   
	   i = Intersection.parse("YIN/K0952S0890");
	   assertNull(i);
   }
}