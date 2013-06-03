package org.deltava.acars;

import junit.framework.TestCase;

public class TestRawTile extends TestCase {

	public void testOverflow() {
		RawTile rt = new RawTile();
		rt.set(1, 1, 5);
		assertEquals(5, rt.getCount(1, 1));
		
		rt.set(2, 1, 150);
		assertEquals(150, rt.getCount(2, 1));
		
		rt.set(3, 1, 254);
		assertEquals(254, rt.getCount(3, 1));
		rt.increment(3, 1);
		assertEquals(255, rt.getCount(3, 1));
		rt.increment(3, 1);
		assertEquals(255, rt.getCount(3, 1));
		
		rt.set(4,  1, 127);
		assertEquals(127, rt.getCount(4, 1));
		rt.increment(4, 1);
		assertEquals(128, rt.getCount(4, 1));
		rt.increment(4, 1);
		assertEquals(129, rt.getCount(4, 1));
	}
}