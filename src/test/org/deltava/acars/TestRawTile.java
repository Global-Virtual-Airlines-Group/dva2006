package org.deltava.acars;

import junit.framework.TestCase;

public class TestRawTile extends TestCase {

	@SuppressWarnings("static-method")
	public void testOverflow() {
		RawTile srt = RawTile.getTile(255);
		assertTrue(srt instanceof SmallRawTile);
		testTile(srt);
		
		srt.set(3, 1, 254);
		assertEquals(254, srt.getCount(3, 1));
		srt.increment(3, 1);
		assertEquals(255, srt.getCount(3, 1));
		srt.increment(3, 1);
		assertEquals(255, srt.getCount(3, 1));
		
		RawTile lrt = RawTile.getTile(2048);
		assertTrue(lrt instanceof LargeRawTile);
		testTile(lrt);
	}
	
	private static void testTile(RawTile rt) {
		rt.set(1, 1, 5);
		assertEquals(5, rt.getCount(1, 1));
		
		rt.set(2, 1, 150);
		assertEquals(150, rt.getCount(2, 1));
		
		rt.set(4,  1, 127);
		assertEquals(127, rt.getCount(4, 1));
		rt.increment(4, 1);
		assertEquals(128, rt.getCount(4, 1));
		rt.increment(4, 1);
		assertEquals(129, rt.getCount(4, 1));
	}
}