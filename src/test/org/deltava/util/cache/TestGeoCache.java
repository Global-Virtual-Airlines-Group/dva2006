package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import java.util.*;

import junit.framework.*;

import org.deltava.beans.schedule.Airline;

public class TestGeoCache extends TestCase {

	private ExpiringGeoCache<Cacheable> _cache;

	public static Test suite() {
		return new CoverageDecorator(TestGeoCache.class, new Class[] { ExpiringGeoCache.class, ExpiringCacheEntry.class });
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_cache = new ExpiringGeoCache<Cacheable>(2, 1, 1);
	}

	@Override
	protected void tearDown() throws Exception {
		_cache = null;
		super.tearDown();
	}

	public void testClone() throws Exception {
		
	}

	public void testCacheOverflow() {
		
	}

	public void testCacheExpiry() throws Exception {
		
	}

	public void testNull() {
		
	}

	public void testLargeCache() {
		Collection<Cacheable> entries = new ArrayList<Cacheable>();
		for (int x = 0; x < 16384; x++)
			entries.add(new CacheableLong(Integer.valueOf(x), x));

		assertEquals(16384, entries.size());
		_cache.setMaxSize(entries.size());
		assertEquals(entries.size(), _cache.getMaxSize());

		long start = System.currentTimeMillis();
		_cache.addAll(entries);
		assertTrue((System.currentTimeMillis() - start) < 500);
		assertEquals(entries.size(), _cache.size());
	}

	public void testMaxSize() {
		_cache.setMaxSize(1);
		assertEquals(0, _cache.size());
		assertEquals(1, _cache.getMaxSize());

		// Add multiple
		Collection<Airline> objs = new ArrayList<Airline>();
		objs.add(new Airline("AF", "Air France"));
		objs.add(new Airline("DVA", "Delta Virtual"));
		objs.add(new Airline("COA", "Continental Virtual"));
		assertTrue(objs.size() > (_cache.getMaxSize() + 1));
		_cache.addAll(objs);
		assertEquals(1, _cache.getMaxSize());
		assertEquals(1, _cache.size());
	}
}