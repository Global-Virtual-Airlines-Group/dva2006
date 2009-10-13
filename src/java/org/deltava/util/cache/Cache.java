// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * An an abstract class to store common cache operations.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class Cache<T extends Cacheable> {

	protected ConcurrentMap<Object, CacheEntry<T>> _cache;
	private final Semaphore _ovLock = new Semaphore(1, true);
	private int _maxSize;

	private final AtomicLong _hits = new AtomicLong();
	private final AtomicLong _gets = new AtomicLong();

	/**
	 * Initializes the cache.
	 * @param maxSize the maximum size of the cache
	 */
	protected Cache(int maxSize) {
		super();
		setMaxSize(maxSize);
		_cache = new ConcurrentHashMap<Object, CacheEntry<T>>(_maxSize + 2, 1); // Set so that rehashes never occur
	}

	/**
	 * Adds a number of entries to the cache.
	 * @param entries a Collection of Cacheable entries
	 */
	public void addAll(Collection<? extends T> entries) {
		for (Iterator<? extends T> i = entries.iterator(); i.hasNext();)
			addEntry(i.next());
		
		checkOverflow();
	}

	/**
	 * Returns if the cache contains a particular cache key.
	 * @param key the cache key
	 * @return TRUE if the cache contains the key, otherwise FALSE
	 */
	public boolean contains(Object key) {
		return _cache.containsKey(key);
	}

	/**
	 * Clears the cache.
	 */
	public void clear() {
		_cache.clear();
	}

	/**
	 * Invalidate a cache entry.
	 * @param key the entry key
	 */
	public void remove(Object key) {
		_cache.remove(key);
	}

	/**
	 * Returns the current size of the cache.
	 * @return the number of entries in the cache
	 */
	public final int size() {
		return _cache.size();
	}

	/**
	 * Returns the maximum size of the cache.
	 * @return the maximum number of entries in the cache
	 */
	public final int getMaxSize() {
		return _maxSize;
	}

	/**
	 * Sets the maximum size of the cache.
	 * @param size the maximum number of entries
	 */
	public final void setMaxSize(int size) {
		_maxSize = Math.max(1, size);
	}

	/**
	 * Automatically resizes the cache in the case of an overflow. This is done by sorting the cache entries using their
	 * natural order, and removing the first entry.
	 */
	protected void checkOverflow() {
		if ((_cache.size() > _maxSize) && _ovLock.tryAcquire()) {
			try {
				TreeSet<CacheEntry<T>> entries = new TreeSet<CacheEntry<T>>(_cache.values());
				while (entries.size() > _maxSize) {
					CacheEntry<T> entry = entries.first();
					_cache.values().remove(entry);
					entries.remove(entry);
				}
			} finally {
				_ovLock.release();
			}
		}
	}

	/**
	 * Log a cache hit. Implementations should call this method from their {@link Cache#get(Object)} method to keep
	 * statistics.
	 * @see Cache#request()
	 * @see Cache#getHits()
	 * @see Cache#getRequests()
	 */
	protected void hit() {
		_hits.incrementAndGet();
	}

	/**
	 * Log a cache request. Implementations should call this method from their {@link Cache#get(Object)} method to keep
	 * statistics.
	 * @see Cache#hit()
	 * @see Cache#getRequests()
	 */
	protected void request() {
		_gets.incrementAndGet();
	}

	/**
	 * Returns the total number of cache hits.
	 * @return the number of hits
	 * @see Cache#getRequests()
	 */
	public final long getHits() {
		return _hits.intValue();
	}

	/**
	 * Returns the total number of cache requests
	 * @return the number of requests
	 * @see Cache#getHits()
	 */
	public final long getRequests() {
		return _gets.intValue();
	}
	
	/**
	 * Adds an entry to the cache.
	 * @param entry
	 */
	protected abstract void addEntry(T entry);

	/**
	 * Adds an entry to the cache.
	 * @param entry the entry to add
	 */
	public final void add(T entry) {
		addEntry(entry);
		checkOverflow();
	}

	/**
	 * Retrieves an entry from the cache.
	 * @param key the cache key
	 * @return the cache entry, or null if the key is not present or the entry is invalid
	 */
	public abstract T get(Object key);
}