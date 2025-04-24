// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A bean to track transaction volume by address and implement rate limiting.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RateLimiter {
	
	private final int _minReqs;
	private final int _minTime;
	private final boolean _doMerge;
	
	private transient final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
	private final Lock _r = _lock.readLock();
	private final Lock _w = _lock.writeLock();
	
	private final Map<String, RequestCounter> _reqs = new HashMap<String, RequestCounter>();
	
	private int _blockCount = Integer.MAX_VALUE;
	private int _blockTime = 600;
	
	/**
	 * Blocking status enumeration.
	 */
	public enum Result {
		PASS, DEGRADE, BLOCK
	}

	/**
	 * Initializes the hunter.
	 * @param doMerge TRUE if requests should be assigned to their parent netblock, otherwise FALSE
	 * @param minRequests the minimum number of requests
	 * @param minTime the minimum amount of time in seconds
	 */
	public RateLimiter(boolean doMerge, int minRequests, int minTime) {
		super();
		_doMerge = doMerge;
		_minReqs = Math.max(1, minRequests);
		_minTime = Math.max(1, minTime);
	}
	
	/**
	 * Loads the limiter with data from an external source.
	 * @param data a Collection of RequestCounters
	 */
	public void load(Collection<RequestCounter> data) {
		try {
			_w.lock();
			_reqs.clear();
			data.forEach(rc -> _reqs.put(rc.getAddress(), rc));
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Returns the minimum number of request required to be categorized as a Spider. 
	 * @return the number of requests
	 */
	public int getMinRequests() {
		return _minReqs;
	}
	
	/**
	 * Returns the minimum time interval for Spider detection.
	 * @return the interval as a Duration
	 */
	public Duration getMinTime() {
		return Duration.ofSeconds(_minTime);
	}
	
	/**
	 * Returns the request counters for each address.
	 * @return the request counters
	 */
	public List<RequestCounter> getCounters() {
		try {
			_r.lock();
			return new ArrayList<RequestCounter>(_reqs.values());
		} finally {
			_r.unlock();
		}
	}
	
	/**
	 * Sets whether the rate limiter should block high-volume clients.
	 * @param reqs the number of requests
	 * @param time the block time in seconds
	 */
	public void setBlocking(int reqs, int time) {
		_blockCount = Math.max(1, reqs);
		_blockTime = Math.max(30, time);
	}
	
	/**
	 * Clears an address' totals.
	 * @param addr the remote address
	 */
	public void remove(String addr) {
		try {
			_w.lock();
			_reqs.remove(addr);
		} finally {
			_w.unlock();
		}
	}
	
	/*
	 * Obtains the counter for a particular remote address, if enabled.
	 * @param add the address
	 */
	public RequestCounter get(String addr) {
		try {
			_r.lock();
			if (_doMerge) {
				Optional<RequestCounter> orc = _reqs.values().stream().filter(rc -> rc.contains(addr)).findAny();
				if (orc.isPresent())
					return orc.get();
			}
			
			return _reqs.get(addr);
		} finally {
			_r.unlock();
		}
	}
	
	/**
	 * Adds a remote address to the hunter.
	 * @param addr the remote address
	 * @return a Result
	 */
	public Result addAddress(String addr) {
		RequestCounter rc = get(addr);
		if (rc == null) {
			rc = new RequestCounter(addr);
			try {
				_w.lock();
				_reqs.put(rc.getAddress(), rc);
			} finally {
				_w.unlock();
			}
		}
				
		// Check if we're a spider
		int reqs = rc.increment();
		if (reqs >= _minReqs) {
			rc.purge(_minTime);
			reqs = rc.getRequests();
			if (reqs > _blockCount) {
				rc.block(_blockTime);
				return Result.BLOCK;
			}
		}
		
		if (reqs >= _minReqs) {
			rc.setDegraded(true);
			return Result.DEGRADE;
		}
		
		rc.setDegraded(false);
		return Result.PASS;
	}
	
	/**
	 * Clears all statistics.
	 */
	public void clear() {
		try {
			_w.lock();
			_reqs.clear();
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Merges request counters based on their network blocks.
	 * @return the merged counters
	 */
	public Collection<RequestCounter> merge() {
		try {
			Collection<RequestCounter> mctrs = new ArrayList<RequestCounter>();
			_w.lock();
			for (RequestCounter rc : _reqs.values()) {
				Optional<RequestCounter> orc = _doMerge ? mctrs.stream().filter(ctr -> ctr.contains(rc.getAddress())).findAny() : Optional.empty();
				if (orc.isPresent()) {
					RequestCounter rc2 = orc.get();
					rc2.merge(rc);
					rc2.setDegraded(rc2.getRequests() >= _minReqs);
				} else
					mctrs.add(rc);
			}
			
			_reqs.clear();
			mctrs.forEach(rc -> _reqs.put(rc.getAddress(), rc));
			return mctrs;
		} finally {
			_w.unlock();
		}
	}

	/**
	 * Purges all statistics exceeding the minimum detection time.
	 */
	public void purge() {
		try {
			_w.lock();
			for (Iterator<Map.Entry<String, RequestCounter>> i = _reqs.entrySet().iterator(); i.hasNext(); ) {
				RequestCounter rc = i.next().getValue();
				rc.purge(_minTime);
				rc.setDegraded(rc.getRequests() > _minReqs);
				if (rc.getRequests() == 0)
					i.remove();
			}
		} finally {
			_w.unlock();
		}
	}
}