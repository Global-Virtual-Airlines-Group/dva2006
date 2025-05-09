package org.deltava.util.cache;

import java.io.File;
import java.time.Instant;
import java.util.*;

import junit.framework.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.Airline;

import org.deltava.util.*;
import org.gvagroup.pool.JedisPool;

public class TestJedisCache extends TestCase {
	
	private Logger log;

	private JedisPool _jedisPool;
	private JedisCache<Cacheable> _cache;
	
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
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestJedisCache.class);
		
		Properties p = new Properties();
		p.setProperty("addr", "192.168.0.2");
		_jedisPool = new JedisPool(1, "TEST");
		_jedisPool.setProperties(p);
		_jedisPool.setLogStack(true);
		_jedisPool.connect(1);
		JedisUtils.init(_jedisPool);
		_cache = new JedisCache<Cacheable>("test", 1);
		assertNotNull(_cache);
		assertTrue(_cache.isRemote());
	}

	@Override
	protected void tearDown() throws Exception {
		_jedisPool.close();
		_cache = null;
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

		Thread.sleep(1550);
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
	
	public void testGetAll() {
		_cache.add(new Airline("AF", "Air France"));
		_cache.add(new Airline("DVA", "Delta Virtual"));
		assertTrue(_cache.contains("AF"));
		assertTrue(_cache.contains("DVA"));
		
		Map<Object, Cacheable> results = _cache.getAll(List.of("AF", "DVA"));
		assertNotNull(results);
		assertEquals(2, results.size());
		assertTrue(results.containsKey("AF"));
		assertTrue(results.containsKey("DVA"));
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