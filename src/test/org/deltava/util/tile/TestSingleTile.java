// Copyright 2006 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.image.BufferedImage;

import java.util.*;

import junit.framework.TestCase;

public class TestSingleTile extends TestCase {
	
	private SingleTile _tile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTile() {
		TileAddress addr = new TileAddress(1, 2, 3);
		assertNotNull(addr);
		_tile = new SingleTile(addr);
		assertEquals(addr, _tile.getAddress());
		assertNotNull(_tile.getImage());
	}
	
	public void testIsEmpty() {
		TileAddress addr = new TileAddress(1, 2, 6);
		assertNotNull(addr);
		_tile = new SingleTile(addr);
		assertNotNull(_tile.getImage());
		assertTrue(_tile.isEmpty());
		
		// Test entire tile
		BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		assertNotNull(img);
		_tile.setImage(img);
		assertEquals(img, _tile.getImage());
		assertTrue(_tile.isEmpty());
		
		// Check descendants
		Collection<TileAddress> descs = addr.getChildren(14);
		for (Iterator<TileAddress> di = descs.iterator(); di.hasNext(); ) {
			TileAddress desc = di.next();
			assertTrue(_tile.isEmpty(desc));
		}
	}
}