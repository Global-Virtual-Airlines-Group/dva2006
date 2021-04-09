// Copyright 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;
import java.time.*;

import org.deltava.beans.*;

/**
 * A bean to track Online Network data feed outages.
 * @author Luke
 * @version 10.0
 * @since 8.6
 */

public class NetworkOutage implements TimeSpan {
	
	private final OnlineNetwork _net;
	private Instant _startTime;
	private Instant _endTime;

	/**
	 * Creates the bean.
	 * @param net the OnlineNetwork 
	 */
	public NetworkOutage(OnlineNetwork net) {
		super();
		_net = net;
	}
	
	/**
	 * Return the Online Network that had an outage.
	 * @return the OnlineNetwork
	 */
	public OnlineNetwork getNetwork() {
		return _net;
	}

	@Override
	public Instant getDate() {
		return _startTime;
	}

	@Override
	public Instant getStartTime() {
		return _startTime;
	}

	@Override
	public Instant getEndTime() {
		return _endTime;
	}
	
	@Override
	public Duration getDuration() {
		return (_endTime == null) ? Duration.between(getStartTime(), Instant.now()) : TimeSpan.super.getDuration();
	}
	
	/**
	 * Updates the start of the outage.
	 * @param dt the start date/time
	 */
	public void setStartTime(Instant dt) {
		_startTime = dt;
	}
	
	/**
	 * Updates the end of the outage.
	 * @param dt the end date/time
	 */
	public void setEndTime(Instant dt) {
		_endTime = dt;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof NetworkOutage) && (compareTo(o) == 0);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_net.toString());
		buf.append('-').append(_startTime);
		buf.append('-').append(_endTime);
		return buf.toString();
	}

	@Override
	public int compareTo(Object o2) {
		NetworkOutage no2 = (NetworkOutage) o2;
		int tmpResult = _net.compareTo(no2._net);
		if (tmpResult == 0)
			tmpResult = _startTime.compareTo(no2._startTime);
		
		return (tmpResult == 0) ? _endTime.compareTo(no2._endTime) : tmpResult;
	}
	
	/**
	 * Creates a list of data feed outages from a Collection of data feed pull times.
	 * @param net the OnlineNetwork 
	 * @param fetchTimes a Collection of pull date/times
	 * @param updateInterval the fetch interval in seconds
	 * @return a Collection of NetworkOutage beans
	 */
	public static Collection<NetworkOutage> calculate(OnlineNetwork net, Collection<Instant> fetchTimes, int updateInterval) {
		Collection<NetworkOutage> results = new ArrayList<NetworkOutage>();
		if (fetchTimes.size() < 2) return results;
		
		List<Instant> ft2 = new ArrayList<Instant>(fetchTimes); Instant lastPull = ft2.get(0);
		for (Instant ft : ft2) {
			long fetchInterval = ft.getEpochSecond() - lastPull.getEpochSecond();
			if (fetchInterval > (updateInterval + 60)) {
				NetworkOutage no = new NetworkOutage(net);
				no.setStartTime(lastPull.plusSeconds(updateInterval));
				no.setEndTime(ft);
				results.add(no);
			}
			
			lastPull = ft;
		}
		
		return results;
	}
}