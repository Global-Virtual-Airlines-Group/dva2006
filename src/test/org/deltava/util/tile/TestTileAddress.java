// Copyright 2006 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;

import junit.framework.TestCase;

import org.gvagroup.tile.TileAddress;

@SuppressWarnings("static-method")
public class TestTileAddress extends TestCase {
	
	public void testLevel1() {
		TileAddress addr = new TileAddress(1, 0, 1);
		assertEquals(1, addr.getX());
		assertEquals(0, addr.getY());
		assertEquals(1, addr.getLevel());
		assertEquals("1", addr.getName());
	}
	
	public void testLevel3() {
		TileAddress addr = new TileAddress(5, 4, 3);
		assertEquals(5, addr.getX());
		assertEquals(4, addr.getY());
		assertEquals(3, addr.getLevel());
		assertEquals("301", addr.getName());
	}
	
	public void testBaseRadarOffset() {
		TileAddress addr = new TileAddress(19, 43, 7);
		assertEquals(19, addr.getX());
		assertEquals(43, addr.getY());
		assertEquals(7, addr.getLevel());
		assertEquals("0212033", addr.getName());
	}
	
	public void testNameLevel1() {
		TileAddress addr = new TileAddress("3");
		assertEquals(1, addr.getX());
		assertEquals(1, addr.getY());
		assertEquals(1, addr.getLevel());
		assertEquals("3", addr.getName());
	}
	
	public void testNameLevel3() {
		TileAddress addr = new TileAddress("301");
		assertEquals(5, addr.getX());
		assertEquals(4, addr.getY());
		assertEquals(3, addr.getLevel());
		assertEquals("301", addr.getName());
	}
	
	public void testNameBaseRadar() {
		TileAddress addr = new TileAddress("0212033");
		assertEquals(19, addr.getX());
		assertEquals(43, addr.getY());
		assertEquals(7, addr.getLevel());
		assertEquals("0212033", addr.getName());
	}
	
	public void testNameRadar() {
		TileAddress addr = new TileAddress(31, 43, 7);
		assertEquals("0213133", addr.getName());
		addr = new TileAddress(19, 51, 7);
		assertEquals("0230033", addr.getName());
		addr = new TileAddress(31, 51, 7);
		assertEquals("0231133", addr.getName());
	}
	
	public void testParent() {
		TileAddress addr = new TileAddress(1, 1, 2);
		assertNotNull(addr.getParent());
		assertEquals(new TileAddress(0,0,1), addr.getParent());
		
		// Do a random check
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			int x = rnd.nextInt(100);
			int y = rnd.nextInt(100);
			int lvl = rnd.nextInt(9) + 2;
			addr = new TileAddress(x, y, lvl);
			assertEquals(new TileAddress(x >> 1, y  >> 1, lvl - 1), addr.getParent());
		}
	}
	
	public void testChildren() {
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			int x = rnd.nextInt(100);
			int y = rnd.nextInt(100);
			int lvl = rnd.nextInt(10) + 1;
			TileAddress addr = new TileAddress(x, y, lvl);
			Collection<TileAddress> children = addr.getChildren();
			assertNotNull(children);
			assertFalse(children.isEmpty());
			assertTrue(children.contains(new TileAddress(x << 1, y << 1, lvl + 1)));
			assertTrue(children.contains(new TileAddress((x << 1) + 1, y << 1, lvl + 1)));
			assertTrue(children.contains(new TileAddress(x << 1, (y << 1) + 1, lvl + 1)));
			assertTrue(children.contains(new TileAddress((x << 1) + 1, (y << 1) + 1, lvl + 1)));
		}
	}
	
	public void testName() {
		final String[] ADDRS = {"0212033", "0213133", "0230033", "0231133"};
		for (int x = 0; x < ADDRS.length; x++) {
			assertNotNull(ADDRS[x]);
			TileAddress addr = new TileAddress(ADDRS[x]);
			System.out.println(ADDRS[x] + " = " + addr);
		}
	}
	
	public void testDescendants() {
		TileAddress addr = new TileAddress(2, 3, 2);
		SortedSet<TileAddress> descs = new TreeSet<TileAddress>(addr.getChildren(4));
		assertNotNull(descs);
		assertEquals(16, descs.size());
		assertEquals(new TileAddress(8, 12, 4), descs.first());
		assertEquals(new TileAddress(11, 15, 4), descs.last());
	}
}