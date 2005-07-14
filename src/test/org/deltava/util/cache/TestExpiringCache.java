package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.Airline;

public class TestExpiringCache extends TestCase {
   
   private ExpiringCache _cache;
   private ExpiringCache.ExpiringCacheEntry _entry;
   
   public static Test suite() {
      return new CoverageDecorator(TestExpiringCache.class, new Class[] { ExpiringCache.class, ExpiringCache.ExpiringCacheEntry.class } );
  }

   protected void setUp() throws Exception {
      super.setUp();
      _cache = new ExpiringCache(2, 1);
   }

   protected void tearDown() throws Exception {
      _cache = null;
      super.tearDown();
   }
   
   public void testCacheEntry() throws Exception {
      Cacheable e1 = new MockCacheable(1);
      Cacheable e2 = new MockCacheable(2);
      _cache.setExpiration(2);
      _entry = _cache.new ExpiringCacheEntry(e1);
      assertSame(e1, _entry.getData());
      _cache.setExpiration(1);
      ExpiringCache.ExpiringCacheEntry entry2 = _cache.new ExpiringCacheEntry(e2);
      assertTrue(_entry.compareTo(entry2) > 0);
   }

   public void testCacheOverflow() {
      Airline dva = new Airline("DVA", "Delta Virtual");
      _cache.add(new Airline("AF", "Air France"));
      _cache.add(dva);
      assertEquals(2, _cache.size());
      assertTrue(_cache.contains("AF"));
      assertTrue(_cache.contains("DVA"));
      assertEquals(_cache.getMaxSize(), _cache.size());
      
      _cache.add(new Airline("COA", "Continental Airlines"));
      assertEquals(2, _cache.size());
      assertFalse(_cache.contains("AF"));
      assertNull(_cache.get("AF"));
      assertTrue(_cache.contains("DVA"));
      assertTrue(_cache.contains("COA"));
      assertSame(dva, _cache.get("DVA"));
   }
   
   public void testValidation() {
      try {
         _cache.setMaxSize(0);
         fail("IllegalArgumentException expected");
      } catch (IllegalArgumentException iae) { }
      
      try {
         _cache.setExpiration(0);
         fail("IllegalArgumentException expected");
      } catch (IllegalArgumentException iae) { }
   }
   
   public void testCacheExpiry() throws Exception {
      _cache.add(new Airline("AF", "Air France"));
      _cache.add(new Airline("DVA", "Delta Virtual"));
      assertTrue(_cache.contains("AF"));
      assertTrue(_cache.contains("DVA"));
      
      Thread.sleep(1050);
      assertNull(_cache.get("AF"));
      assertNull(_cache.get("DVA"));
   }
}