// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An object cache that supports creation dates. The major difference between this cache and a
 * {@link ExpiringCache} is that this cache does not purge an entry until the cache overflows, whereas
 * an {@link ExpiringCache} invalidates data based on age. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AgingCache extends Cache {
   
   protected long _lastCreationTime;
   
   protected class AgingCacheEntry implements Comparable {
      
      private Cacheable _entry;
      private long _createdOn;
      
      public AgingCacheEntry(Cacheable entry) {
         super();
         _entry = entry;
         long now = System.currentTimeMillis();
         _createdOn = (now <= _lastCreationTime) ? ++_lastCreationTime : now;
         _lastCreationTime = _createdOn;
      }
      
      public Cacheable getData() {
         return _entry;
      }
      
      public long getCreationTime() {
         return _createdOn;
      }
      
      public int compareTo(Object o2) {
         AgingCacheEntry e2 = (AgingCacheEntry) o2;
         return new Long(_createdOn).compareTo(new Long(e2.getCreationTime()));
      }
   }
   
   /**
    * Creates a new aging cache.
    * @param maxSize the maximum size of the cache
    * @throws IllegalArgumentException if maxSize is zero or negative
    * @see AgingCache#setMaxSize(int)
    */
   public AgingCache(int maxSize) {
      super(maxSize);
   }
   
   /**
    * Adds an entry to the cache. If this operation would cause the cache to exceed its maximum size,
    * then the entry with the earliest creation date will be removed.
    * @param entry the entry to add to the cache 
    */
   public void add(Cacheable entry) {

      // Create the cache entry
      AgingCacheEntry e = new AgingCacheEntry(entry);
      _cache.put(entry.cacheKey(), e);

      // Check for overflow
      checkOverflow();
   }

   /**
    * Returns an entry from the cache.
    * @param key the cache key
    * @return the cache entry, or null if not present
    */
   public Cacheable get(Object key) {
      AgingCacheEntry entry = (AgingCacheEntry) _cache.get(key);
      return (entry == null) ? null : entry.getData();
   }
}