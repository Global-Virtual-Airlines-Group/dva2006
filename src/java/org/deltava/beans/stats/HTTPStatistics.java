// Copyright 2005, 2009, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

/**
 * A class for storing daily HTTP server statistics.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class HTTPStatistics implements java.io.Serializable, Comparable<HTTPStatistics> {

	private final Instant _date;
	private int _reqs;
	private int _homeHits;
	private long _execTime;
	private long _backEndTime;
	private long _bandwidth;

	/**
	 * Create a new Statistics entry for a particular date.
	 * @param dt the Date
	 * @see HTTPStatistics#getDate()
	 */
	public HTTPStatistics(Instant dt) {
		super();
		_date = dt;
	}

	/**
	 * Returns the date of these HTTP statistics.
	 * @return the Date of the statistics
	 */
	public Instant getDate() {
		return _date;
	}

	/**
	 * Returns the number of HTTP requests.
	 * @return the number of requests
	 * @see HTTPStatistics#setRequests(int)
	 */
	public int getRequests() {
		return _reqs;
	}

	/**
	 * Returns the number of Home Page HTTP requests.
	 * @return the number of home page requests
	 * @see HTTPStatistics#setHomePageHits(int)
	 */
	public int getHomePageHits() {
		return _homeHits;
	}

	/**
	 * Returns the total time serving requests.
	 * @return the time in milliseconds
	 * @see HTTPStatistics#setExecutionTime(long)
	 */
	public long getExecutionTime() {
		return _execTime;
	}

	/**
	 * Returns the total time executing database operations.
	 * @return the time in milliseconds
	 * @see HTTPStatistics#setBackEndTime(long)
	 */
	public long getBackEndTime() {
		return _backEndTime;
	}

	/**
	 * Returns the total number of bytes served.
	 * @return the number of bytes served
	 * @see HTTPStatistics#setBandwidth(long)
	 */
	public long getBandwidth() {
		return _bandwidth;
	}

	/**
	 * Updates the number of HTTP requests.
	 * @param reqs the total number of requests
	 * @see HTTPStatistics#getRequests()
	 */
	public void setRequests(int reqs) {
		_reqs = reqs;
	}

	/**
	 * Updates the number of home page HTTP requests.
	 * @param hits the number of home page requests
	 * @see HTTPStatistics#getHomePageHits()
	 */
	public void setHomePageHits(int hits) {
		_homeHits = hits;
	}

	/**
	 * Updates the total serving time.
	 * @param time the total time serving requests in milliseconds
	 * @see HTTPStatistics#getExecutionTime()
	 */
	public void setExecutionTime(long time) {
		_execTime = time;
	}
	
	/**
	 * Updates the total database time.
	 * @param time the total database execution time in milliseconds
	 * @see HTTPStatistics#getBackEndTime()
	 */
	public void setBackEndTime(long time) {
		_backEndTime = time;
	}

	/**
	 * Updates the total bandwidth used.
	 * @param bytes the number of bytes served
	 * @see HTTPStatistics#getBandwidth()
	 */
	public void setBandwidth(long bytes) {
		_bandwidth = bytes;
	}
	
	@Override
	public int hashCode() {
		return _date.hashCode();
	}

	/**
	 * Compares two statistics objects by comparing their date.
	 */
	@Override
	public int compareTo(HTTPStatistics s2) {
		return _date.compareTo(s2._date);
	}
}