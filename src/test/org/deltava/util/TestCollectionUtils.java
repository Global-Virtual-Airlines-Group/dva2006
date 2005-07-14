// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestCollectionUtils extends TestCase {

   public static Test suite() {
      return new CoverageDecorator(TestCollectionUtils.class, new Class[] { CollectionUtils.class } );
  }
   
   private class ID {
      
      private int _id;
      
      public ID(int id) {
         super();
         _id = id;
      }
      
      public int getID() {
         return _id;
      }
   }
   
   public void testIsEmpty() {
      assertTrue(CollectionUtils.isEmpty(null));
      assertTrue(CollectionUtils.isEmpty(Collections.EMPTY_LIST));
      assertTrue(CollectionUtils.isEmpty(new ArrayList()));
      
      List testList = new ArrayList();
      testList.add("test");
      assertFalse(CollectionUtils.isEmpty(testList));
   }
   
   public void testHasDelta() {
      Collection c1 = Arrays.asList(new String[] {"1", "2", "3"});
      Collection c2 = Arrays.asList(new String[] {"3", "4", "5"});
      List c3 = Arrays.asList(new String[] {"1", "2", "3", "4"});
      
      assertTrue(CollectionUtils.hasDelta(c1, c2));
      assertTrue(CollectionUtils.hasDelta(c1, c3));
      assertFalse(CollectionUtils.hasDelta(c1, c3.subList(0, 3)));
   }
   
   public void testGetDelta() {
      Collection c1 = Arrays.asList(new String[] {"1", "2", "3"});
      Collection c2 = Arrays.asList(new String[] {"3", "4", "5"});

      Collection cd1 = CollectionUtils.getDelta(c1, c2);
      assertEquals(2, cd1.size());
      assertTrue(cd1.contains("1"));
      assertTrue(cd1.contains("2"));
      
      Collection cd2 = CollectionUtils.getDelta(c2, c1);
      assertEquals(2, cd2.size());
      assertTrue(cd2.contains("4"));
      assertTrue(cd2.contains("5"));
   }
   
   public void testSetDelta() {
      // We do the double-list creation since arrays.asList returns an immutable collection
      Collection c1 = new ArrayList(Arrays.asList(new String[] {"1", "2", "3"}));
      Collection c2 = new ArrayList(Arrays.asList(new String[] {"3", "4", "5"}));
      
      CollectionUtils.setDelta(c1, c2);
      assertEquals(2, c1.size());
      assertTrue(c1.contains("1"));
      assertTrue(c1.contains("2"));
      
      assertEquals(2, c2.size());
      assertTrue(c2.contains("4"));
      assertTrue(c2.contains("5"));
   }
   
   public void testLoadList() {
      String[] entries = {"1", "2", "3"};
      Collection toList = CollectionUtils.loadList(entries, null);
      assertEquals(entries.length, toList.size());
      
      assertNull(CollectionUtils.loadList(null, null));
      assertEquals(toList, CollectionUtils.loadList(null, toList));
   }
   
   public void testCreateMap() {
      Set ids = new HashSet();
      ids.add(new ID(1));
      ids.add(new ID(2));
      ids.add(new ID(10));
      ids.add(new Object());
      
      Map m = CollectionUtils.createMap(ids, "ID");
      assertEquals(ids.size() - 1, m.size());
      assertTrue(m.containsKey(new Integer(1)));
      assertTrue(m.containsKey(new Integer(2)));
      assertTrue(m.containsKey(new Integer(10)));
   }
}