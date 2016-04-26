package org.deltava.acars;

import java.util.concurrent.BlockingQueue;

public class PlotGlobalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 3;
	private static final int MAX_ZOOM = 9;
	private static final int MAX_TILES = 350 * 1024;
	
	private final RouteEntryFilter _f = new AllFilter(MAX_ZOOM);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_zooms.put(Integer.valueOf(2), new ProjectInfo(2, 3072, 96));
		_zooms.put(Integer.valueOf(3), new ProjectInfo(3, 1536, 48));
		_zooms.put(Integer.valueOf(4), new ProjectInfo(4, 768, 40));
		_zooms.put(Integer.valueOf(5), new ProjectInfo(5, 512, 33));
		_zooms.put(Integer.valueOf(6), new ProjectInfo(6, 288, 30));
		_zooms.put(Integer.valueOf(7), new ProjectInfo(7, 192, 24));
		_zooms.put(Integer.valueOf(8), new ProjectInfo(8, 128, 16));
		_zooms.put(Integer.valueOf(9), new ProjectInfo(9, 64, 12));
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 48, 8));
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