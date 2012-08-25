// Copyright 2008, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * A cache for File handles.
 * @author Luke
 * @version 5.0
 * @since 2.2
 */

public class FileCache extends Cache<CacheableFile> {
	
	private static final Logger log = Logger.getLogger(FileCache.class);
	
	private long _maxAge;
	
	protected class FileCacheEntry extends CacheEntry<CacheableFile> {
		private final long _lastModified;
		
		public FileCacheEntry(CacheableFile f) {
			super(f);
			_lastModified = f.lastModified();
		}
		
		public boolean exists() {
			return (get() != null) && (get().exists());
		}
		
		public long getLastUpdateTime() {
			return _lastModified;
		}
		
		public int compareTo(CacheEntry<CacheableFile> e2) {
			FileCacheEntry fe2 = (FileCacheEntry) e2;
			return new Long(getLastUpdateTime()).compareTo(new Long(fe2.getLastUpdateTime()));
		}
	}
	
	/**
	 * Initializes the cache.
	 * @param maxSize the maximum number of entries in the cache
	 */
	public FileCache(int maxSize) {
		super(maxSize);
	}
	
	/**
	 * Adds an entry to the cache.
	 * @param obj the file to add to the cache
	 */
	protected void addEntry(CacheableFile obj) {
		if (obj == null)
			return;

		FileCacheEntry e = new FileCacheEntry(obj);
		_cache.put(obj.cacheKey(), e);
	}
	
	/**
	 * Not implemented.
	 */
	protected void addNullEntry(Object key) {
		// NOOP
	}
	
	/**
	 * Invalidates a cache entry and deletes the file from the filesystem.
	 * @param key the entry key
	 */
	public final void remove(Object key) {
		FileCacheEntry entry = (FileCacheEntry) _cache.get(key);
		_cache.remove(key);
		if (entry != null) {
			if (!entry.exists() || !entry.get().delete())
				log.warn("Cannot delete file " + entry.get().getAbsolutePath());
		}
	}
	
	/**
	 * Clears the cache and deletes any cached files.
	 */
	public final void clear() {
		for (Iterator<CacheEntry<CacheableFile>> i = _cache.values().iterator(); i.hasNext(); ) {
			FileCacheEntry entry = (FileCacheEntry) i.next();
			if (entry.exists()) {
				if (!entry.get().delete())
					log.warn("Cannot delete " + entry.get().getAbsolutePath());
			}
			
			i.remove();
		}
	}
	
	/**
	 * Sets the maximum age of a file before it should be expired (and deleted) when queried.
	 * @param age the age in minutes or 0 for no expiration
	 */
	public void setMaxAge(int age) {
		_maxAge = Math.max(0, age) * 60000L;
	}
	
	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @return the file, or null if not present
	 */
	public CacheableFile get(Object key) {
		request();
		if (key == null)
			return null;
		
		// Check if the file exists or is new
		FileCacheEntry entry = (FileCacheEntry) _cache.get(key);
		if (entry == null)
			return null;

		// Check if expired
		if (_maxAge > 0) {
			long age = System.currentTimeMillis() - entry.getLastUpdateTime();
			if ((age >= _maxAge) || !entry.exists()) {
				remove(key);
				return null;
			}
		}
			
		return entry.get();
	}
}