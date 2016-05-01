// Copyright 2012, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.acars;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.*;

import org.gvagroup.tile.TileAddress;

public class SparseGlobalTile {
	
	private final int _zoom;
	
	private final LongAdder _hits = new LongAdder();
	private final LongAdder _reqs = new LongAdder();
	
	private final ReentrantReadWriteLock _rw = new ReentrantReadWriteLock();
	private final Lock _r = _rw.readLock();
	private final Lock _w = _rw.writeLock();
	
	private final TileL1Cache _l1;
	final Map<TileAddress, TileData> _l2 = new LinkedHashMap<TileAddress, TileData>();

	private class TileL1Cache extends LinkedHashMap<TileAddress, TileData> {
		
		private final int _max;
		
		TileL1Cache(int maxSize) {
			super(maxSize+4, 1.0f);
			_max = maxSize;
		}
		
		public int getMaxSize() {
			return _max;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<TileAddress, TileData> eldest) {
			boolean doRemove = (size() >= _max);
			if (doRemove) {
				//System.out.println(Thread.currentThread().getName() + " Saving "+ eldest.getKey());
				TileData td = eldest.getValue();
				td.save();
				_l2.put(eldest.getKey(), td);
			}
			
			return doRemove;
		}
	}
	
	public SparseGlobalTile(int zoom, int size) {
		_zoom = zoom;
		_l1 = new TileL1Cache(size);
	}

	public int getZoom() {
		return _zoom;
	}
	
	public int getMaxSize() {
		return _l1.getMaxSize();
	}
	
	public int size() {
		return _l1.size() + _l2.size();
	}
	
	public String getCacheInfo() {
		float cacheInfo = _hits.longValue() * 100f /_reqs.longValue();
		return "Info = " + _hits.longValue() + " / " + _reqs.longValue() + " = " + cacheInfo + "%";
	}
	
	public void plot(int x, int y) {
		TileAddress addr = TileAddress.fromPixel(x, y, _zoom);
		TileData td = getTile(addr, true);
		
		RawTile img = td.getImage();
		int ix = (x & 0xFF); int iy = (y & 0xFF);
		img.increment(ix, iy);
	}
	
	void put(TileAddress addr, TileData td) {
		try {
			_w.lock();
			_l1.put(addr, td);
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Lists the Tile Addresses with data. The tiles are returned with already loaded tiles first.
	 * @return a Collection of TileAddress beans
	 */
	public Collection<TileAddress> getTiles() {
		LinkedList<TileAddress> results = new LinkedList<TileAddress>();
		try {
			_r.lock();
			results.addAll(_l1.keySet());
			results.addAll(_l2.keySet());
		} finally {
			_r.unlock();
		}
		
		return results;
	}
	
	/**
	 * Removes the data for a tile.
	 * @param addr the TileAddress
	 */
	public void remove(TileAddress addr) {
		TileData td = null;
		try {
			_w.lock();
			td = _l1.get(addr);
			if (td == null) {
				td = _l2.get(addr);
				if (td != null)
					_l2.remove(addr);
			} else
				_l1.remove(addr);
		} finally {
			_w.unlock();
			if (td != null)
				td.flush();
		}
	}
	
	TileData getTile(TileAddress addr, boolean makeNew) {
		
		_reqs.increment();
		try {
			_r.lock();
			TileData td = _l1.get(addr);
			if (td != null) {
				_hits.increment();
				return td; 
			}
			
			td = _l2.get(addr);
			if (td != null) {
				//System.out.println(Thread.currentThread().getName() + " Loading " + addr);
				try {
					_r.unlock();
					_w.lock();
					if (td.isSwapped())
						td.load();
					
					_l1.put(addr, td);
					_l2.remove(addr);
				} finally {
					_r.lock();
					_w.unlock();
				}
			} else if (makeNew) {
				td = new TileData();
				_hits.increment();
				try {
					_r.unlock();
					_w.lock();
					_l1.put(addr, td);
				} finally {
					_r.lock();
					_w.unlock();
				}
			}
			
			return td;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		} finally {
			_r.unlock();
		}
	}
}