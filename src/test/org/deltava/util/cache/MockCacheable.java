// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.cache;

class MockCacheable implements Cacheable {
   
   protected int _id;
   
   public MockCacheable(int id) {
      _id = id;
   }
   
   public Object cacheKey() {
      return new Integer(_id);
   }
}