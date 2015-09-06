package org.deltava.util.cache;

import java.util.*;

import junit.framework.*;

import org.apache.log4j.PropertyConfigurator;
import org.deltava.beans.schedule.Airline;
import org.deltava.util.MemcachedUtils;

public class TestMemcachedCache extends TestCase {

	private MemcachedCache<Cacheable> _cache;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		MemcachedUtils.init(Collections.singletonList("192.168.0.2:11211"));
		_cache = new MemcachedCache<Cacheable>("testSuite", 1);
		assertNotNull(_cache);
	}

	protected void tearDown() throws Exception {
		_cache = null;
		MemcachedUtils.shutdown();
		super.tearDown();
	}

	public void testClone() throws Exception {
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		_cache.add(o1);
		Cacheable o2 = _cache.get(Integer.valueOf(1));
		assertNotNull(o2);
		assertEquals(o1, o2);
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

	public void testNull() {
		_cache.addNull("DVA");
		assertTrue(_cache.contains("DVA"));
		assertNull(_cache.get("DVA"));
	}

	public void testLargeCache() throws Exception {
		Collection<Cacheable> entries = new ArrayList<Cacheable>();
		for (int x = 0; x < 16384; x++)
			entries.add(new CacheableLong(Integer.valueOf(x), x));

		long start = System.currentTimeMillis();
		_cache.addAll(entries);
		Thread.sleep(500);
		assertTrue((System.currentTimeMillis() - start) < 1000);
	}
}