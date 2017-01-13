package org.deltava.acars;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.*;

import org.deltava.util.StringUtils;

public class PlotGlobalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 2;
	private static final int MAX_ZOOM = 9;
	private static final int MAX_TILES = 350 * 1024;
	
	private final RouteEntryFilter _f = new AllFilter(MAX_ZOOM);
	
	public static void main(String[] args) throws Exception {
		// Init Log4j
		PropertyConfigurator.configure("log4j.properties");
		log = Logger.getLogger(PlotMap.class);
		
		setUp();
		
		if (args.length > 1) {
			int readWorkers = StringUtils.parse(args[1], READ_THREADS);
			log.info("Using " + readWorkers + " read threads");
			READ_THREADS = readWorkers;
			WRITE_THREADS = readWorkers;
		}
		
		boolean isLocal = (args.length > 0) && ("local".equalsIgnoreCase(args[0]));
		if (!isLocal) {
			PlotGlobalMap plotter = new PlotGlobalMap();
			log.info("Generating Global Map");
			plotter.plotMap();
		} else {
			PlotLocalMap lPlotter = new PlotLocalMap();
			log.info("Generating Local Map");
			if (args.length > 3)
				lPlotter.plotMap(StringUtils.parse(args[2], 0), StringUtils.parse(args[3], 40));
			else
				lPlotter.plotMap(0, 40);
		}
		
		LogManager.shutdown();
	}
	
	PlotGlobalMap() {
		_zooms.put(Integer.valueOf(2), new ProjectInfo(2, 4096, 96));
		_zooms.put(Integer.valueOf(3), new ProjectInfo(3, 2048, 56));
		_zooms.put(Integer.valueOf(4), new ProjectInfo(4, 1024, 48));
		_zooms.put(Integer.valueOf(5), new ProjectInfo(5, 512, 40));
		_zooms.put(Integer.valueOf(6), new ProjectInfo(6, 256, 32));
		_zooms.put(Integer.valueOf(7), new ProjectInfo(7, 144, 24));
		_zooms.put(Integer.valueOf(8), new ProjectInfo(8, 128, 20));
		_zooms.put(Integer.valueOf(9), new ProjectInfo(9, 96, 16));
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 64, 12));
	}
	
	public void plotMap() throws Exception {
		
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