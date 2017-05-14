package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import java.util.*;

import junit.framework.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.GeoPosition;

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
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		GeoLocation loc = new GeoPosition(44.5, -85.5);
		_cache.add(loc, o1);
		assertEquals(1, _cache.size());
		Cacheable o2 = _cache.get(loc);
		assertNotNull(o2);
		assertSame(o1, o2);
	}

	public void testCacheOverflow() {
		_cache.add(new GeoPosition(44.5,-85.5), new Airline("DVA", "Delta Virtual"));
		_cache.add(new GeoPosition(52.5, 5.5), new Airline("AFV", "Air France Virtual"));
		assertEquals(_cache.getMaxSize(), _cache.size());
		_cache.add(new GeoPosition(42.5,-85.5), new Airline("DVA", "Delta Virtual"));
		assertEquals(_cache.getMaxSize(), _cache.size());
		assertTrue(_cache.contains(new GeoPosition(52.5, 5.5)));
		assertTrue(_cache.contains(new GeoPosition(42.5, -85.5)));
	}

	public void testCacheExpiry() throws Exception {
		_cache.add(new GeoPosition(44.5,-85.5), new Airline("DVA", "Delta Virtual"));
		_cache.add(new GeoPosition(52.5, 5.5), new Airline("AFV", "Air France Virtual"));
		assertEquals(_cache.getMaxSize(), _cache.size());
		Thread.sleep(1050);
		assertFalse(_cache.contains(new GeoPosition(52.5, 5.5)));
		assertFalse(_cache.contains(new GeoPosition(42.5, -85.5)));
	}

	public void testNull() {
		_cache.addNull(new GeoPosition(52.5, 5.5));
		assertEquals(1, _cache.size());
		assertTrue(_cache.contains(new GeoPosition(52.5, 5.5)));
	}
	
	public void testRounding() {
		_cache.addNull(new GeoPosition(52.5, 5.5));
		assertEquals(1, _cache.size());
		assertTrue(_cache.contains(new GeoPosition(52.51, 5.51)));
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