// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.ViewEntry;

/**
 * A bean to track Spider requests.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RequestCounter implements java.io.Serializable, ViewEntry {
	
	private final String _addr;
	private final List<Long> _reqs = new ArrayList<Long>();
	private long _blockTime;
	
	private IPBlock _ipInfo;

	/**
	 * Initializes the counter.
	 * @param addr the remote address
	 */
	public RequestCounter(String addr) {
		super();
		_addr = addr;
	}
	
	/**
	 * Clones a request counter.
	 * @param rc the RequestCounter
	 */
	public RequestCounter(RequestCounter rc) {
		this(rc.getAddress());
		_reqs.addAll(rc._reqs);
		_blockTime = rc._blockTime;
		_ipInfo = rc._ipInfo;
	}

	/**
	 * Increments the request counter.
	 * @return the number of requests
	 */
	public synchronized int increment() {
		_reqs.add(Long.valueOf(System.currentTimeMillis()));
		return _reqs.size();
	}

	/**
	 * Blocks this address for a specified period of time.
	 * @param duration the duration in seconds
	 */
	public void block(int duration) {
		_blockTime = System.currentTimeMillis() + (duration * 1000);
	}
	
	/**
	 * Returns the remote address.
	 * @return the address
	 */
	public String getAddress() {
		return _addr;
	}
	
	/**
	 * Returns information about this remote address' net block.
	 * @return an IPBlock
	 */
	public IPBlock getIPInfo() {
		return _ipInfo;
	}
	
	/**
	 * Returns the date/time of the oldest request.
	 * @return the date/time of the request, or null if none
	 */
	public Instant getOldest() {
		return _reqs.isEmpty() ? null : Instant.ofEpochMilli(_reqs.getFirst().longValue());
	}
	
	/**
	 * Returns the date/time of the newest request.
	 * @return the date/time of the request, or null if none
	 */
	public Instant getNewest() {
		return _reqs.isEmpty() ? null : Instant.ofEpochMilli(_reqs.getLast().longValue());
	}
	
	/**
	 * Returns the number of requests for this address.
	 * @return the number of requests
	 */
	public synchronized int getRequests() {
		return _reqs.size();
	}
	
	/**
	 * Mereges the totals of two counters together. If the second counter is blocked, the later block time will be propagated.
	 * @param rc a RequestCounter
	 */
	public synchronized void merge(RequestCounter rc) {
		_reqs.addAll(rc._reqs);
		Collections.sort(_reqs);
		_blockTime = Math.max(_blockTime, rc._blockTime);
	}
	
	/**
	 * Updates information about this remote address' net block.
	 * @param ip an IPInfo bean
	 */
	public void setIPInfo(IPBlock ip) {
		_ipInfo = ip;
	}
	
	public boolean contains(String addr) {
		return (_ipInfo != null) && _ipInfo.contains(addr);
	}
	
	/**
	 * Purges all access times more than a certain period of time old.
	 * @param maxTime the time interval in seconds
	 */
	public synchronized void purge(int maxTime) {
		long now = System.currentTimeMillis();
		if (_blockTime < now)
			_blockTime = 0;
		
		if (_reqs.isEmpty()) return;
		final long minTime = now - (maxTime * 1000);
		_reqs.removeIf(t -> t.longValue() < minTime);
	}

	@Override
	public String getRowClassName() {
		return (_blockTime > 0) ? "error" : null;
	}
}