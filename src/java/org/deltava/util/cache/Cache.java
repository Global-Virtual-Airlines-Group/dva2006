// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.cache;

import java.util.*;
import java.lang.reflect.Method;

/**
 * An an abstract class to store common cache operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Cache implements java.io.Serializable {
   
   protected Map _cache;
   private int _maxSize;
   
   private int _hits;
   private int _gets;

   /**
    * Initializes the cache.
    * @param maxSize the maximum size of the cache
    * @throws IllegalArgumentException if maxSize is zero or negative
    */
   protected Cache(int maxSize) {
      super();
      setMaxSize(maxSize);
      _cache = new HashMap(_maxSize + 2, 1); // Set so that rehashes never occur
   }
   
   /**
    * Adds a number of entries to the cache.
    * @param entries a Collection of Cacheable entries
    */
   public void addAll(Collection entries) {
      for (Iterator i = entries.iterator(); i.hasNext(); ) {
         Object entry = i.next();
         if (entry instanceof Cacheable)
            add((Cacheable) entry);
      }
   }
   
   /**
    * Returns if the cache contains a particular cache key.
    * @param key the cache key
    * @return TRUE if the cache contains the key, otherwise FALSE
    */
   public synchronized boolean contains(Object key) {
      return _cache.containsKey(key);
   }
   
   /**
    * Invalidate a cache entry.
    * @param key the entry key
    */
   public synchronized void remove(Object key) {
      _cache.remove(key);
   }
   
   /**
    * Returns the current size of the cache.
    * @return the number of entries in the cache
    */
   public synchronized final int size() {
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
    * @throws IllegalArgumentException if size is zero or negative
    */
   public final void setMaxSize(int size) {
      if (size < 1) throw new IllegalArgumentException("Invalid size - " + size);

      _maxSize = size;
   }
   
   /**
    * Automatically resizes the cache in the case of an overflow. This is done by sorting the
    * cache entries using their natural order, and removing the first entry.
    */
   protected void checkOverflow() {
      if (_cache.size() > _maxSize) {
         TreeSet entries = new TreeSet(_cache.values());
         _cache.values().remove(entries.first());
      }
   }
   
   /**
    * Calls {@link Object#clone()} on a Cloneable object to create a shallow-copy.
    * @param src the source Object
    * @return a clone of src, or src if cloning not supported
    */
   protected Cacheable getClone(Cacheable src) {
      try {
         Method m = src.getClass().getDeclaredMethod("clone", (Class []) null);
         Object clone = m.invoke(src, (Object []) null);
         return (clone instanceof Cacheable) ? (Cacheable) clone : src;
      } catch (Exception e) {
         return src;
      }
   }
   
   /**
    * Log a cache hit. Implementations should call this method from their {@link Cache#get(Object)}
    * method to keep statistics.
    * @see Cache#request()
    * @see Cache#getHits()
    * @see Cache#getRequests()
    */
   protected void hit() {
      _hits++;
   }
   
   /**
    * Log a cache request. Implementations should call this method from their {@link Cache#get(Object)} 
    * method to keep statistics.
    * @see Cache#hit()
    * @see Cache#getRequests()
    */
   protected void request() {
      _gets++;
   }
   
   /**
    * Returns the total number of cache hits.
    * @return the number of hits
    * @see Cache#getRequests()
    */
   public final int getHits() {
      return _hits;
   }
   
   /**
    * Returns the total number of cache requests
    * @return the number of requests
    * @see Cache#getHits()
    */
   public final int getRequests() {
      return _gets;
   }
   
   /**
    * Adds an entry to the cache.
    * @param entry the entry to add
    */
   public abstract void add(Cacheable entry);
   
   /**
    * Retrieves an entry from the cache. 
    * @param key the cache key
    * @return the cache entry, or null if the key is not present or the entry is invalid
    */
   public abstract Cacheable get(Object key);
}