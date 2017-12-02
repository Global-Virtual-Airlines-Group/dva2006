package org.deltava.beans.acars;

import java.time.Instant;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.GeoPosition;

public class TestRouteEntry extends AbstractBeanTestCase {
   
   private RouteEntry _entry;
   private Instant _dt;
   
   public static Test suite() {
      return new CoverageDecorator(TestRouteEntry.class, new Class[] { RouteEntry.class } );
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _dt = Instant.now();
      _entry = new ACARSRouteEntry(_dt, new GeoPosition(45.6789, -112.2334));
      setBean(_entry);
   }

   @Override
protected void tearDown() throws Exception {
      _entry = null;
      _dt = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(_dt, _entry.getDate());
      assertNotNull(_entry.getLocation());
      assertEquals(45.6789, _entry.getLocation().getLatitude(), 0.0001);
      assertEquals(-112.2334, _entry.getLocation().getLongitude(), 0.0001);
      checkProperty("altitude", Integer.valueOf(30045));
      checkProperty("heading", Integer.valueOf(241));
      checkProperty("airSpeed", Integer.valueOf(251));
      checkProperty("groundSpeed", Integer.valueOf(275));
      checkProperty("verticalSpeed", Integer.valueOf(-175));
      checkProperty("n1", Double.valueOf(75.1));
      checkProperty("n2", Double.valueOf(85.1));
   }
   
   public void testValidation() {
      validateInput("altitude", Integer.valueOf(-301), IllegalArgumentException.class);
      validateInput("altitude", Integer.valueOf(100001), IllegalArgumentException.class);
      validateInput("heading", Integer.valueOf(-1), IllegalArgumentException.class);
      validateInput("heading", Integer.valueOf(361), IllegalArgumentException.class);
      validateInput("airSpeed", Integer.valueOf(-21), IllegalArgumentException.class);
      validateInput("airSpeed", Integer.valueOf(701), IllegalArgumentException.class);
      validateInput("groundSpeed", Integer.valueOf(-21), IllegalArgumentException.class);
      validateInput("groundSpeed", Integer.valueOf(1501), IllegalArgumentException.class);
      validateInput("verticalSpeed", Integer.valueOf(-7001), IllegalArgumentException.class);
      validateInput("verticalSpeed", Integer.valueOf(7001), IllegalArgumentException.class);
      validateInput("n1", Double.valueOf(-0.1), IllegalArgumentException.class);
      validateInput("n1", Double.valueOf(115.1), IllegalArgumentException.class);
      validateInput("n2", Double.valueOf(-0.1), IllegalArgumentException.class);
      validateInput("n2", Double.valueOf(115.1), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      RouteEntry e2 = new ACARSRouteEntry(_dt.plusSeconds(1), new GeoPosition(1, 1));
      assertTrue(e2.getDate().toEpochMilli() > _entry.getDate().toEpochMilli());
      assertTrue(e2.compareTo(_entry) > 0);
   }
}