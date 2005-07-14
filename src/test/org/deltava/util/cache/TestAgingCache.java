package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.Airline;

public class TestAgingCache extends TestCase {
   
   private AgingCache _cache;
   private AgingCache.AgingCacheEntry _entry;
   
   public static Test suite() {
      return new CoverageDecorator(TestAgingCache.class, new Class[] { AgingCache.class, AgingCache.AgingCacheEntry.class } );
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _cache = new AgingCache(2);
   }

   protected void tearDown() throws Exception {
      _cache = null;
      super.tearDown();
   }
   
   public void testCacheEntry() throws Exception {
      Cacheable e1 = new MockCacheable(1);
      Cacheable e2 = new MockCacheable(2);
      _entry = _cache.new AgingCacheEntry(e1);
      assertSame(e1, _entry.getData());
      Thread.sleep(20);
      AgingCache.AgingCacheEntry entry2 = _cache.new AgingCacheEntry(e2);
      assertTrue(_entry.compareTo(entry2) < 0);
   }

   public void testCache() {
      _cache.add(new Airline("AF", "Air France"));
      Airline dva = new Airline("DVA", "Delta Virtual");
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
   }
}