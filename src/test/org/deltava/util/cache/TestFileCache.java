package org.deltava.util.cache;

import java.io.File;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.schedule.Airline;

public class TestFileCache extends TestCase {

	private File _f;
	private FileCache<Cacheable> _cache;

	private AgingCache<Cacheable>.AgingCacheEntry<Cacheable> _entry;
	private AgingCache<Cacheable>.AgingNullCacheEntry<Cacheable> _nullEntry;

	public static Test suite() {
		return new CoverageDecorator(TestFileCache.class, new Class[] { FileCache.class, AgingCache.AgingCacheEntry.class });
	}

	@Override
   protected void setUp() throws Exception {
      super.setUp();
      _f = new File(System.getProperty("java.io.tmpdir"), "FileCache.dat");
      assertFalse(_f.exists());
      _cache = new FileCache<Cacheable>(2, _f);
      assertFalse(_cache.isRemote());
   }

	@Override
	protected void tearDown() throws Exception {
		_cache = null;
		_f.delete();
		super.tearDown();
	}

	public void testCacheEntry() throws Exception {
		Cacheable e1 = new CacheableLong(Integer.valueOf(1), 1);
		Cacheable e2 = new CacheableLong(Integer.valueOf(2), 2);
		_entry = _cache.new AgingCacheEntry<Cacheable>(e1);
		assertSame(e1, _entry.get());
		Thread.sleep(20);
		AgingCache<Cacheable>.AgingCacheEntry<Cacheable> entry2 = _cache.new AgingCacheEntry<Cacheable>(e2);
		assertTrue(_entry.compareTo(entry2) < 0);
	}

	public void testNullCacheEntry() {
		_nullEntry = _cache.new AgingNullCacheEntry<Cacheable>();
		assertNull(_nullEntry.get());
	}

	public void testClone() {
		Cacheable o1 = new CacheableLong(Integer.valueOf(1), 1);
		_cache.add(o1);
		assertEquals(1, _cache.size());
		Cacheable o2 = _cache.get(Integer.valueOf(1));
		assertNotNull(o2);
		assertSame(o1, o2);
	}

	public void testCache() {
		_cache.add(new Airline("AF", "Air France"));
		Airline dva = new Airline("DVA", "Delta Virtual");
		_cache.add(dva);
		assertTrue(_f.exists());
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

	public void testNull() {
		_cache.addNull("DVA");
		assertEquals(1, _cache.size());
		assertTrue(_cache.contains("DVA"));
		assertNull(_cache.get("DVA"));
	}
}