// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestNDB extends AbstractBeanTestCase {

   private NDB _ndb;
   
   public static Test suite() {
      return new CoverageDecorator(TestNDB.class, new Class[] { NDB.class, NavigationDataBean.class });
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _ndb = new NDB(12.345, -23.456);
      setBean(_ndb);
   }

   @Override
protected void tearDown() throws Exception {
      _ndb = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(12.345, _ndb.getLatitude(), 0.0001);
      assertEquals(-23.456, _ndb.getLongitude(), 0.0001);
      assertEquals(Navaid.NDB, _ndb.getType());
      
      assertNotNull(_ndb.getPosition());
      assertEquals(_ndb.getLatitude(), _ndb.getPosition().getLatitude(), 0.0001);
      assertEquals(_ndb.getLongitude(), _ndb.getPosition().getLongitude(), 0.0001);
      
      checkProperty("code", "TST");
      checkProperty("name", "Test VOR");
      checkProperty("frequency", "123.35");
      
      assertEquals(_ndb.getCode().hashCode(), _ndb.hashCode());
      assertEquals(_ndb.getCode(), _ndb.cacheKey());
   }
   
   public void testValidation() {
      validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
      validateInput("type", Integer.valueOf(11), IllegalArgumentException.class);
   }
   
   public void testComparator() {
      _ndb.setCode("TST");
      
      NDB ndb2 = new NDB(12.346, -25.1632);
      ndb2.setCode("ABC");
      
      assertTrue(_ndb.compareTo(ndb2) > 0);
      assertTrue(ndb2.compareTo(_ndb) < 0);
      assertFalse(_ndb.equals(ndb2));
      
      ndb2.setCode("TST");
      assertTrue(_ndb.equals(ndb2));
      assertFalse(_ndb.equals(new Object()));
      assertFalse(_ndb.equals(null));
   }
}