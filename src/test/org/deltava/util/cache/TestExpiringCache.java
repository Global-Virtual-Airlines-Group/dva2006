package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.Airline;

public class TestExpiringCache extends TestCase {

	private ExpiringCache<Cacheable> _cache;
	private ExpiringCache<Cacheable>.ExpiringCacheEntry<Cacheable> _entry;

	public static Test suite() {
		return new CoverageDecorator(TestExpiringCache.class, new Class[] { ExpiringCache.class, ExpiringCache.ExpiringCacheEntry.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		_cache = new ExpiringCache<Cacheable>(2, 1);
	}

	protected void tearDown() throws Exception {
		_cache = null;
		super.tearDown();
	}

	public void testCacheEntry() throws Exception {
		Cacheable e1 = new CacheableLong(Integer.valueOf(1), 1);
		Cacheable e2 = new CacheableLong(Integer.valueOf(2), 2);
		_cache.setExpiration(2);
		_entry = _cache.new ExpiringCacheEntry<Cacheable>(e1);
		assertSame(e1, _entry.getData());
		_cache.setExpiration(1);
		ExpiringCache<Cacheable>.ExpiringCacheEntry<Cacheable> entry2 = _cache.new ExpiringCacheEntry<Cacheable>(e2);
		assertTrue(_entry.compareTo(entry2) > 0);
	}

	public void testClone() throws Exception {
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		_cache.add(o1);
		assertEquals(1, _cache.size());
		Cacheable o2 = _cache.get(new Integer(1));
		assertNotNull(o2);
		assertSame(o1, o2);
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

	public void testCacheExpiry() throws Exception {
		_cache.add(new Airline("AF", "Air France"));
		_cache.add(new Airline("DVA", "Delta Virtual"));
		assertTrue(_cache.contains("AF"));
		assertTrue(_cache.contains("DVA"));
		assertEquals(2, _cache.getMaxSize());
		assertEquals(2, _cache.size());

		Thread.sleep(1050);
		assertNull(_cache.get("AF"));
		assertNull(_cache.get("DVA"));

		_cache.setMaxSize(1);
		assertEquals(0, _cache.size());
		assertEquals(1, _cache.getMaxSize());
		_cache.add(new Airline("AF", "Air France"));
		assertEquals(1, _cache.getMaxSize());
		assertEquals(1, _cache.size());
		_cache.add(new Airline("DVA", "Delta Virtual"));
		assertEquals(1, _cache.getMaxSize());
		assertEquals(1, _cache.size());
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