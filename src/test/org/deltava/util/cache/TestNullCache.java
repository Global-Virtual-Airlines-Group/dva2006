package org.deltava.util.cache;

import java.util.*;

import org.hansel.CoverageDecorator;

import junit.framework.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

public class TestNullCache extends TestCase {
   
   private NullCache<Cacheable> _cache;
   
   public static Test suite() {
      return new CoverageDecorator(TestNullCache.class, new Class[] { NullCache.class } );
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _cache = new NullCache<Cacheable>();
   }

   @Override
protected void tearDown() throws Exception {
      _cache = null;
      super.tearDown();
   }
   
   public void testCache() {
      _cache.add(new Airline("AF", "Air France"));
      Airline dva = new Airline("DVA", "Delta Virtual");
      _cache.add(dva);
      assertEquals(0, _cache.size());
      assertFalse(_cache.contains("AF"));
      assertFalse(_cache.contains("DVA"));
      assertEquals(_cache.getMaxSize(), _cache.size());
      
      _cache.add(new Airline("COA", "Continental Airlines"));
      assertEquals(0, _cache.size());
      assertFalse(_cache.contains("AF"));
      assertNull(_cache.get("AF"));
      assertFalse(_cache.contains("DVA"));
      assertFalse(_cache.contains("COA"));
   }
   
   public void testNull() {
	   _cache.addNull("DVA");
	   assertEquals(0, _cache.size());
	   assertFalse(_cache.contains("DVA"));
	   assertNull(_cache.get("DVA"));
   }
   
   public void testGeo() {
	   GeoLocation loc = new GeoPosition(15, -25);
	   assertFalse(_cache.contains(loc));
	   _cache.add(loc, new CacheableLong(loc, 0));
	   assertFalse(_cache.contains(loc));
	   assertNull(_cache.get(loc));
   }
   
   public void testLargeCache() {
	   Collection<Cacheable> entries = new ArrayList<Cacheable>();
	   for (int x = 0; x < 16384; x++)
		   entries.add(new CacheableLong(Integer.valueOf(x), x));
	   
	   assertEquals(16384, entries.size());
	   _cache.setMaxSize(entries.size());
	   assertEquals(0, _cache.getMaxSize());
	   
	   long start = System.currentTimeMillis();
	   _cache.addAll(entries);
	   assertTrue((System.currentTimeMillis() - start) < 500);
	   assertEquals(0, _cache.size());
   }
}