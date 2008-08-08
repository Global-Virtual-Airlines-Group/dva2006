package org.deltava.beans.acars;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.GeoPosition;

public class TestRouteEntry extends AbstractBeanTestCase {
   
   private RouteEntry _entry;
   private Date _dt;
   
   public static Test suite() {
      return new CoverageDecorator(TestRouteEntry.class, new Class[] { RouteEntry.class } );
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _dt = new Date();
      _entry = new RouteEntry(_dt, new GeoPosition(45.6789, -112.2334));
      setBean(_entry);
   }

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
      checkProperty("altitude", new Integer(30045));
      checkProperty("heading", new Integer(241));
      checkProperty("airSpeed", new Integer(251));
      checkProperty("groundSpeed", new Integer(275));
      checkProperty("verticalSpeed", new Integer(-175));
      checkProperty("n1", new Double(75.1));
      checkProperty("n2", new Double(85.1));
   }
   
   public void testValidation() {
      validateInput("altitude", new Integer(-301), IllegalArgumentException.class);
      validateInput("altitude", new Integer(100001), IllegalArgumentException.class);
      validateInput("heading", new Integer(-1), IllegalArgumentException.class);
      validateInput("heading", new Integer(361), IllegalArgumentException.class);
      validateInput("airSpeed", new Integer(-21), IllegalArgumentException.class);
      validateInput("airSpeed", new Integer(701), IllegalArgumentException.class);
      validateInput("groundSpeed", new Integer(-21), IllegalArgumentException.class);
      validateInput("groundSpeed", new Integer(1501), IllegalArgumentException.class);
      validateInput("verticalSpeed", new Integer(-7001), IllegalArgumentException.class);
      validateInput("verticalSpeed", new Integer(7001), IllegalArgumentException.class);
      validateInput("n1", new Double(-0.1), IllegalArgumentException.class);
      validateInput("n1", new Double(115.1), IllegalArgumentException.class);
      validateInput("n2", new Double(-0.1), IllegalArgumentException.class);
      validateInput("n2", new Double(115.1), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      RouteEntry e2 = new RouteEntry(new Date(_dt.getTime() + 5), new GeoPosition(1, 1));
      assertTrue(e2.getDate().getTime() > _entry.getDate().getTime());
      assertTrue(e2.compareTo(_entry) > 0);
   }
}
