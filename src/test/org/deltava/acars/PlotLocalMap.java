package org.deltava.acars;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.system.SystemData;

public class PlotLocalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 10;
	private static final int MAX_ZOOM = 12;
	private static final int MAX_TILES = 163840;
	
	private static final List<String> _airports = Arrays.asList("ATL", "JFK", "LAX", "SLC", "YYZ", "MIA", "MSP", "DTW", "BOS", "SEA", "LAS", "SAN", "SFO", "NRT", 
			"MCO", "DFW", "IAD", "CDG", "AMS", "HKX", "HKG", "HNL", "OGG", "CVG", "YUL", "LHR", "ICN", "BDA", "YVR", "SYD"); 
	
	protected void setUp() throws Exception {
		super.setUp();
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 30, 3));
		_zooms.put(Integer.valueOf(11), new ProjectInfo(11, 22, 3));
		_zooms.put(Integer.valueOf(12), new ProjectInfo(12, 20, 3));
	}
	
	public void testPlotMap() throws Exception {
		
		// Init the filter
		Collection<Airport> airports = new ArrayList<Airport>();
		for (String iata : _airports)
			airports.add(SystemData.getAirport(iata));
		
		AirportEntryFilter f = new AirportEntryFilter(MAX_ZOOM, 75, airports);
		f.setMaxAltitude(10000);
		
		// Load flight IDs
		log.info("Loading Flight IDs");
		BlockingQueue<Integer> IDs = getIDs(0, _airports);
		
		// Load flights - separate thread per flight
		SparseGlobalTile gt = new SparseGlobalTile(MAX_ZOOM, MAX_TILES);
		loadFlights(IDs, gt, f);
		
		while (gt.getZoom() >= MIN_ZOOM) {
			SparseGlobalTile pgt = scale(gt);
			gt = pgt;
		}
	}
}