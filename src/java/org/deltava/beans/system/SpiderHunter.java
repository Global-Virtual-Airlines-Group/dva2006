// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Duration;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A bean to detect Spiders based on usage within a period of time.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class SpiderHunter {
	
	private final int _minReqs;
	private final int _minTime;
	
	private transient final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
	private final Lock _r = _lock.readLock();
	private final Lock _w = _lock.writeLock();
	
	private final Map<String, RequestCounter> _reqs = new HashMap<String, RequestCounter>();

	/**
	 * Initializes the hunter.
	 * @param minRequests the minimum number of requests
	 * @param minTime the minimum amount of time in seconds 
	 */
	public SpiderHunter(int minRequests, int minTime) {
		super();
		_minReqs = Math.max(1, minRequests);
		_minTime = Math.max(1, minTime);
	}
	
	/**
	 * Returns the minimum number of request required to be categorized as a Spider. 
	 * @return the number of requests
	 */
	public int getRequests() {
		return _minReqs;
	}
	
	/**
	 * Returns the minimum time interval for Spider detection.
	 * @return the interval as a Duration
	 */
	public Duration getMinTime() {
		return Duration.ofSeconds(_minTime);
	}
	
	public void remove(String addr) {
		try {
			_w.lock();
			_reqs.remove(addr);
		} finally {
			_w.unlock();
		}
	}
	
	/*
	 * Obtains the counter for a particular remote address.
	 * @param add the address
	 */
	public RequestCounter get(String addr) {
		try {
			_r.lock();
			return _reqs.get(addr);
		} finally {
			_r.unlock();
		}
	}
	
	/**
	 * Adds a remote address to the hunter.
	 * @param addr the remote address
	 * @return TRUE if the address is possibly a Spider, otherwise FALSE
	 * @see SpiderHunter#isSpider(String)
	 */
	public boolean addAddress(String addr) {
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
		}
		
		return (reqs >= _minReqs);
	}
	
	/**
	 * Returns whether a particular remote address is possibly a Spider.
	 * @param addr the remote address
	 * @return TRUE if the address is possibly a Spider, otherwise FALSE
	 */
	public boolean isSpider(String addr) {
		RequestCounter rc = get(addr);
		if ((rc == null) || (rc.getRequests() < _minReqs)) return false;
		
		// This may not be a true positive, so do a purge just in case
		rc.purge(_minTime);
		return (rc.getRequests() >= _minReqs);
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
	 * Purges all statistics exceeding the minimum detection time.
	 */
	public void purge() {
		try {
			_w.lock();
			for (Iterator<Map.Entry<String, RequestCounter>> i = _reqs.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, RequestCounter> me = i.next();
				RequestCounter rc = me.getValue();
				rc.purge(_minTime);
				if (rc.getRequests() == 0)
					i.remove();
			}
			
		} finally {
			_w.unlock();
		}
	}
}