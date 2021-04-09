package org.deltava.util.cache;

import java.time.Instant;
import java.util.*;

import junit.framework.*;

import org.apache.log4j.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.util.RedisUtils;
import org.deltava.util.TaskTimer;

public class TestRedisCache extends TestCase {
	
	private Logger log;

	private RedisCache<Cacheable> _cache;
	
	private static class ExpiringCacheableLong extends CacheableLong implements ExpiringCacheable {
		
		private final Instant _expDate;

		public ExpiringCacheableLong(Object cacheKey, long value, Instant expiryDate) {
			super(cacheKey, value);
			_expDate = expiryDate;
		}

		@Override
		public Instant getExpiryDate() {
			return _expDate;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestRedisCache.class);
		RedisUtils.init("192.168.0.2", 2, "JUnit");
		_cache = new RedisCache<Cacheable>("test", 1);
		assertNotNull(_cache);
	}

	@Override
	protected void tearDown() throws Exception {
		_cache = null;
		RedisUtils.shutdown();
		super.tearDown();
	}

	public void testClone() throws Exception {
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		_cache.add(o1);
		Cacheable o2 = _cache.get(Integer.valueOf(1));
		assertNotNull(o2);
		assertEquals(o1, o2);
	}

	public void testCacheOverflow() {
		Airline dva = new Airline("DVA", "Delta Virtual");
		_cache.add(new Airline("AF", "Air France"));
		_cache.add(dva);
		assertTrue(_cache.contains("AF"));
		assertTrue(_cache.contains("DVA"));

		_cache.add(new Airline("COA", "Continental Airlines"));
		assertTrue(_cache.contains("AF"));
		assertTrue(_cache.contains("DVA"));
		assertTrue(_cache.contains("COA"));
		assertEquals(dva, _cache.get("DVA"));
	}

	public void testCacheExpiry() throws Exception {
		_cache.add(new Airline("AF", "Air France"));
		_cache.add(new Airline("DVA", "Delta Virtual"));
		assertTrue(_cache.contains("AF"));
		assertTrue(_cache.contains("DVA"));
		assertNotNull(_cache.get("AF"));
		assertNotNull(_cache.get("DVA"));
		assertEquals(2, _cache.getRequests());
		assertEquals(2, _cache.getHits());

		Thread.sleep(1050);
		assertNull(_cache.get("AF"));
		assertNull(_cache.get("DVA"));
		assertEquals(4, _cache.getRequests());
		assertEquals(2, _cache.getHits());
	}

	public void testNull() {
		_cache.addNull("DVA");
		assertTrue(_cache.contains("DVA"));
		assertNull(_cache.get("DVA"));
	}

	public void testLargeCache() throws Exception {
		Instant expiryDate = Instant.now().plusSeconds(30);
		Collection<Cacheable> entries = new ArrayList<Cacheable>();
		for (int x = 0; x < 8192; x++)
			entries.add(new ExpiringCacheableLong(Integer.valueOf(x), x, expiryDate));

		TaskTimer tt = new TaskTimer();
		_cache.addAll(entries);
		log.info(entries.size() + " entries added in " + tt.stop() + "ms");
		
		tt.start();
		assertEquals(entries.size(), _cache.size());
		log.info("Cache sized in " + tt.stop() + "ms");

		tt.start();
		_cache.clear();
		log.info("Cache cleared in " + tt.stop() + "ms");
	}
}