package org.deltava.util.cache;

import org.hansel.CoverageDecorator;

import java.time.Instant;
import java.util.*;

import junit.framework.*;

import org.deltava.beans.schedule.Airline;

public class TestExpiringCache extends TestCase {

	private ExpiringCache<Cacheable> _cache;
	private ExpiringCacheEntry<Cacheable> _entry;
	private ExpiringCache<Cacheable>.ExpiringNullCacheEntry<Cacheable> _nullEntry;
	
	private static class ExpiringCacheableLong extends CacheableLong implements ExpiringCacheable {
		private final Instant _expTime = Instant.now().plusSeconds(2);

		public ExpiringCacheableLong(Object cacheKey, long value) {
			super(cacheKey, value);
		}

		@Override
		public Instant getExpiryDate() {
			return _expTime;
		}
	}

	public static Test suite() {
		return new CoverageDecorator(TestExpiringCache.class, new Class[] { ExpiringCache.class, ExpiringCacheEntry.class });
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_cache = new ExpiringCache<Cacheable>(2, 1);
	}

	@Override
	protected void tearDown() throws Exception {
		_cache = null;
		super.tearDown();
	}

	public void testCacheEntry() throws Exception {
		Cacheable e1 = new CacheableLong(Integer.valueOf(1), 1);
		Cacheable e2 = new CacheableLong(Integer.valueOf(2), 2);
		_entry = _cache.new ExpiringLocalCacheEntry<Cacheable>(e1);
		assertSame(e1, _entry.get());
		ExpiringCacheEntry<Cacheable> entry2 = _cache.new ExpiringLocalCacheEntry<Cacheable>(e2);
		assertTrue(_entry.compareTo(entry2) < 0);
	}

	public void testNullCacheEntry() {
		Object key = "$key";
		_nullEntry = _cache.new ExpiringNullCacheEntry<Cacheable>(key);
		assertEquals(key, _nullEntry.toString());
		assertNull(_nullEntry.get());
	}

	public void testClone() throws Exception {
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		_cache.add(o1);
		assertEquals(1, _cache.size());
		Cacheable o2 = _cache.get(Integer.valueOf(1));
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

	public void testNull() {
		_cache.addNull("DVA");
		assertEquals(1, _cache.size());
		assertTrue(_cache.contains("DVA"));
		assertNull(_cache.get("DVA"));
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
	
	public void testExpiringCacheable() {
		assertEquals(0, _cache.size());
		
		CacheableLong cl = new ExpiringCacheableLong("FOO", 10);
		assertTrue(cl instanceof ExpiringCacheable);
		_cache.add(cl);
		
		assertEquals(1, _cache.size());
		CacheableLong cl2 = (CacheableLong) _cache.get("FOO");
		assertNotNull(cl2);
		assertEquals(cl.getValue(), cl2.getValue());
		assertTrue(cl2 instanceof ExpiringCacheable);
	}
}