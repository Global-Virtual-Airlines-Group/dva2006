package org.deltava.util.cache;

import java.util.ArrayList;
import java.util.Collection;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.Airline;

public class TestAgingCache extends TestCase {
   
   private AgingCache<Cacheable> _cache;
   private AgingCache<Cacheable>.AgingCacheEntry<Cacheable> _entry;
   
   public static Test suite() {
      return new CoverageDecorator(TestAgingCache.class, new Class[] { AgingCache.class, AgingCache.AgingCacheEntry.class } );
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _cache = new AgingCache<Cacheable>(2);
   }

   protected void tearDown() throws Exception {
      _cache = null;
      super.tearDown();
   }
   
   public void testCacheEntry() throws Exception {
	  Cacheable e1 = new CacheableLong(Integer.valueOf(1), 1);
	  Cacheable e2 = new CacheableLong(Integer.valueOf(2), 2);
      _entry = _cache.new AgingCacheEntry<Cacheable>(e1);
      assertSame(e1, _entry.getData());
      Thread.sleep(20);
      AgingCache<Cacheable>.AgingCacheEntry<Cacheable> entry2 = _cache.new AgingCacheEntry<Cacheable>(e2);
      assertTrue(_entry.compareTo(entry2) < 0);
   }
   
   public void testClone() throws Exception {
      Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
      _cache.add(o1);
      assertEquals(1, _cache.size());
      Cacheable o2 = _cache.get(new Integer(1));
      assertNotNull(o2);
      assertSame(o1, o2);
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
   
   public void testLargeCache() {
	   Collection<Cacheable> entries = new ArrayList<Cacheable>();
	   for (int x = 0; x < 8192; x++)
		   entries.add(new CacheableLong(Integer.valueOf(x), x));
	   
	   assertEquals(8192, entries.size());
	   _cache.setMaxSize(entries.size());
	   assertEquals(entries.size(), _cache.getMaxSize());
	   
	   long start = System.currentTimeMillis();
	   _cache.addAll(entries);
	   assertTrue((System.currentTimeMillis() - start) < 500);
	   assertEquals(entries.size(), _cache.size());
   }
}