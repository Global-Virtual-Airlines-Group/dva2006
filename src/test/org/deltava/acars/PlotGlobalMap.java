package org.deltava.acars;

import java.util.concurrent.BlockingQueue;

public class PlotGlobalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 3;
	private static final int MAX_ZOOM = 9;
	private static final int MAX_TILES = 350 * 1024;
	
	private final RouteEntryFilter _f = new AllFilter(MAX_ZOOM);
	
	protected void setUp() throws Exception {
		super.setUp();
		_zooms.put(Integer.valueOf(2), new ProjectInfo(2, 1024, 128));
		_zooms.put(Integer.valueOf(3), new ProjectInfo(3, 512, 76));
		_zooms.put(Integer.valueOf(4), new ProjectInfo(4, 420, 56));
		_zooms.put(Integer.valueOf(5), new ProjectInfo(5, 360, 32));
		_zooms.put(Integer.valueOf(6), new ProjectInfo(6, 256, 20));
		_zooms.put(Integer.valueOf(7), new ProjectInfo(7, 144, 14));
		_zooms.put(Integer.valueOf(8), new ProjectInfo(8, 80, 10));
		_zooms.put(Integer.valueOf(9), new ProjectInfo(9, 48, 6));
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 32, 5));
	}
	
	public void testPlotMap() throws Exception {
		
		// Load flight IDs
		BlockingQueue<Integer> IDs = getIDs(0);
		
		// Load flights - separate thread per flight
		SparseGlobalTile gt = new SparseGlobalTile(MAX_ZOOM, MAX_TILES);
		loadFlights(IDs, gt, _f);
		
		// Truncate the tile table
		truncateTracks();
		
		// Write tracks
		while (gt.getZoom() >= MIN_ZOOM) {
			SparseGlobalTile pgt = scale(gt);
			gt = pgt;
		}
	}
}