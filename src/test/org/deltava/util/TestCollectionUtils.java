package org.deltava.util;

import java.util.*;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestCollectionUtils extends TestCase {

	class ID {
		private final int _id;

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
		assertTrue(CollectionUtils.isEmpty(new ArrayList<Object>()));

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

	public void testCreateMap() {
		Set<ID> ids = new HashSet<ID>();
		ids.add(new ID(1));
		ids.add(new ID(2));
		ids.add(new ID(10));

		Map<?, ?> m = CollectionUtils.createMap(ids, ID::getID);
		assertEquals(ids.size() - 1, m.size());
		assertTrue(m.containsKey(Integer.valueOf(1)));
		assertTrue(m.containsKey(Integer.valueOf(2)));
		assertTrue(m.containsKey(Integer.valueOf(10)));
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