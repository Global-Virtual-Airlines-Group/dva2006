// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.cache;

public class MockCloneableCacheable extends MockCacheable {

   public MockCloneableCacheable(int id) {
      super(id);
   }

   public final Object clone() {
      return new MockCloneableCacheable(_id);
   }
}