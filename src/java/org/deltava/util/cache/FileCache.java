// Copyright 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * A cache for File handles.
 * @author Luke
 * @version 3.1
 * @since 2.2
 */

public class FileCache extends Cache<CacheableFile> {
	
	private static final Logger log = Logger.getLogger(FileCache.class);
	
	private long _maxAge;
	
	protected static class FileCacheEntry extends CacheEntry<CacheableFile> {
		
		public FileCacheEntry(CacheableFile f) {
			super(f);
		}
		
		public long getLastUpdateTime() {
			return getData().lastModified();
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

		// Create the cache entry
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
			if (!entry.getData().delete())
				log.warn("Cannot delete " + entry.getData().getAbsolutePath());
		}
	}
	
	/**
	 * Clears the cache and deletes any cached files.
	 */
	public final void clear() {
		for (Iterator<CacheEntry<CacheableFile>> i = _cache.values().iterator(); i.hasNext(); ) {
			CacheEntry<CacheableFile> entry = i.next();
			if (entry.getData().exists()) {
				if (!entry.getData().delete())
					log.warn("Cannot delete " + entry.getData().getAbsolutePath());
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
		if ((entry == null) || (!entry.getData().exists()))
			return null;

		// Check if expired
		if (_maxAge > 0) {
			long age = System.currentTimeMillis() - entry.getLastUpdateTime();
			if (age >= _maxAge) {
				remove(key);
				return null;
			}
		}
			
		return entry.getData();
	}
}