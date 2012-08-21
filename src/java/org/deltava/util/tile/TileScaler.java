// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * A utility class to handle multi-threaded tile scaling.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class TileScaler implements Runnable {
	
	private static final Logger log = Logger.getLogger(TileScaler.class);

	private final SparseGlobalTile _native;
	private SparseGlobalTile _dest;
	private final int _maxThreads;
	
	private class ScaleWorker implements Runnable {
		private final SparseGlobalTile _src;
		private final SparseGlobalTile _dst;
		private final TileAddress _addr;
		
		ScaleWorker(SparseGlobalTile src, SparseGlobalTile dst, TileAddress addr) {
			_src = src;
			_dst = dst;
			_addr = addr;
		}
		
		@Override
		public void run() {
			SingleTile st = _src.get(_addr);
			if (!st.isEmpty())
				_dst.add(st);
		}
	}
	
	/**
	 * Initializes the Scaler pool.
	 * @param maxThreads the maximum number of worker threads
	 * @param gt the native SparseGlobalTile
	 */
	public TileScaler(int maxThreads, SparseGlobalTile gt) {
		super();
		_native = gt;
		_maxThreads = Math.max(1, maxThreads);
	}
	
	/**
	 * Returns the SparseGlobalTile with the results.
	 * @return a SparseGlobalTile, or null if not run yet
	 */
	public SparseGlobalTile getResults() {
		return _dest;
	}

	@Override
	public void run() {
		if (_native.getLevel() < 1)
			throw new IllegalStateException("Already at minimum zoom");
		else if (_dest != null)
			return;
		
		// Create the destination
		_dest = new SparseGlobalTile(_native.getLevel() - 1);
		_dest.setDynamicPalette(_native.hasDynamicPalette());
		
		// Build the thread pol
		ThreadPoolExecutor exec = new ThreadPoolExecutor(_maxThreads, _maxThreads, 150, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);
		
		// Get parents and create work
		Collection<TileAddress> parents = new HashSet<TileAddress>();
		for (TileAddress addr : _native.getAddresses()) {
			TileAddress parent = addr.getParent();
			if (parents.add(parent))
				exec.execute(new ScaleWorker(_native, _dest, parent));
		}

		exec.shutdown();
		try {
			exec.awaitTermination(45, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			log.warn("Excessive scaler pool time!");
		}
	}
}