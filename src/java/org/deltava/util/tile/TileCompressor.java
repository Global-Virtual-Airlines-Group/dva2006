// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * A utility class to handle multi-threaded tile compression.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class TileCompressor implements Runnable {
	
	private static final Logger log = Logger.getLogger(TileCompressor.class);
	
	private final Collection<SingleTile> _tiles = new ArrayList<SingleTile>();
	protected final BlockingQueue<PNGTile> _results = new LinkedBlockingQueue<PNGTile>();
	private final int _maxThreads;
	
	private class PNGWorker implements Runnable {
		private final SingleTile _src;
		
		PNGWorker(SingleTile src) {
			_src = src;
		}
		
		@Override
		public void run() {
			_results.add(new PNGTile(_src));
		}
	}

	/**
	 * Initializes the Compressor pool.
	 * @param maxThreads the maximum number of worker threads
	 * @param layers the SparseGlobalTiles to convert
	 * 
	 */
	public TileCompressor(int maxThreads, Collection<SparseGlobalTile> layers) {
		super();
		_maxThreads = Math.max(1, maxThreads);
		for (SparseGlobalTile sgt : layers)
			_tiles.addAll(sgt.getTiles());
	}
	
	/**
	 * Returns the results of the compressor pool. 
	 * @return a Collection of PNGTile objects
	 */
	public Collection<PNGTile> getResults() {
		return _results;
	}

	@Override
	public void run() {
		
		// Build the thread pol
		ThreadPoolExecutor exec = new ThreadPoolExecutor(_maxThreads, _maxThreads, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);
		
		// Get parents and create work
		for (SingleTile st : _tiles)
			exec.execute(new PNGWorker(st));
		
		exec.shutdown();
		try {
			exec.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			log.warn("Excessive compressor pool time!");
		}
	}
}