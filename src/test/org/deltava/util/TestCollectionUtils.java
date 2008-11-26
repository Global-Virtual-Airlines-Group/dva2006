package org.deltava.util;

import java.util.*;

import junit.framework.TestCase;

public class TestCollectionUtils extends TestCase {

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

		List<String> testList = new ArrayList<String>();
		testList.add("test");
		assertFalse(CollectionUtils.isEmpty(testList));
	}

	public void testHasDelta() {
		Collection<String> c1 = Arrays.asList(new String[] { "1", "2", "3" });
		Collection<String> c2 = Arrays.asList(new String[] { "3", "4", "5" });
		List<String> c3 = Arrays.asList(new String[] { "1", "2", "3", "4" });

		assertTrue(CollectionUtils.hasDelta(c1, c2));
		assertTrue(CollectionUtils.hasDelta(c1, c3));
		assertFalse(CollectionUtils.hasDelta(c1, c3.subList(0, 3)));
	}

	public void testGetDelta() {
		Collection<String> c1 = Arrays.asList(new String[] { "1", "2", "3" });
		Collection<String> c2 = Arrays.asList(new String[] { "3", "4", "5" });

		Collection<String> cd1 = CollectionUtils.getDelta(c1, c2);
		assertEquals(2, cd1.size());
		assertTrue(cd1.contains("1"));
		assertTrue(cd1.contains("2"));

		Collection<String> cd2 = CollectionUtils.getDelta(c2, c1);
		assertEquals(2, cd2.size());
		assertTrue(cd2.contains("4"));
		assertTrue(cd2.contains("5"));
	}

	public void testSetDelta() {
		// We do the double-list creation since arrays.asList returns an immutable collection
		Collection<String> c1 = new ArrayList<String>(Arrays.asList(new String[] { "1", "2", "3" }));
		Collection<String> c2 = new ArrayList<String>(Arrays.asList(new String[] { "3", "4", "5" }));

		CollectionUtils.setDelta(c1, c2);
		assertEquals(2, c1.size());
		assertTrue(c1.contains("1"));
		assertTrue(c1.contains("2"));

		assertEquals(2, c2.size());
		assertTrue(c2.contains("4"));
		assertTrue(c2.contains("5"));
	}

	public void testLoadList() {
		String[] entries = { "1", "2", "3" };
		Collection<String> toList = CollectionUtils.loadList(entries, null);
		assertEquals(entries.length, toList.size());
		assertEquals(toList, CollectionUtils.loadList(null, toList));
	}

	public void testCreateMap() {
		Set<Object> ids = new HashSet<Object>();
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

	public void testHasMatches() {
		Set<String> c1 = new HashSet<String>();
		c1.add("A");
		c1.add("B");
		c1.add("C");
		Set<String> c2 = new HashSet<String>();
		c2.add("A");
		c2.add("C");
		c2.add("D");
		assertEquals(2, CollectionUtils.hasMatches(c1, c2));
		c2.remove("C");
		assertEquals(1, CollectionUtils.hasMatches(c1, c2));
		c1.remove("A");
		assertEquals(0, CollectionUtils.hasMatches(c1, c2));
	}

	public void testGetLast() {
		Set<String> s1 = new LinkedHashSet<String>();
		s1.add("A");
		s1.add("B");
		assertEquals("B", CollectionUtils.getLast(s1));
	}

	public void testUnion() {
		Collection<String> c1 = new ArrayList<String>(Arrays.asList(new String[] { "1", "2", "3" }));
		Collection<String> c2 = new ArrayList<String>(Arrays.asList(new String[] { "3", "4", "5" }));

		Collection<String> u = CollectionUtils.union(c1, c2);
		assertNotNull(u);
		assertEquals(1, u.size());
		assertTrue(u.contains("3"));
		
		c1 = new ArrayList<String>(Arrays.asList(new String[] { "1", "2", "3", "5"}));
		c2 = new ArrayList<String>(Arrays.asList(new String[] { "3", "4", "5" }));
		
		u = CollectionUtils.union(c1, c2);
		assertNotNull(u);
		assertEquals(2, u.size());
		assertTrue(u.contains("3"));
		assertTrue(u.contains("5"));
		
		c1 = new ArrayList<String>(Arrays.asList(new String[] { "1", "2"}));
		c2 = new ArrayList<String>(Arrays.asList(new String[] { "3", "4", "5" }));
		
		u = CollectionUtils.union(c1, c2);
		assertNotNull(u);
		assertEquals(0, u.size());
	}
}