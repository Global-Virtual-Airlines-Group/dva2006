// Copyright 2005, 2007, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.time.Instant;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store servlet scoreboard entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ServletScoreboardEntry implements java.io.Serializable, Comparable<ServletScoreboardEntry>, ViewEntry {

	private final String _threadName;
	private String _remoteAddr;
	private String _remoteHost;
	
	private long _startTime;
	private long _endTime;
	private long _execCount;
	private boolean _isAlive = true;
	
	private String _url;
	
	/**
	 * Creates a new Scoreboard Entry.
	 * @param threadName the thread name
	 * @see ServletScoreboardEntry#getName()
	 * @see ServletScoreboardEntry#getStartTime()
	 */
	public ServletScoreboardEntry(String threadName) {
		super();
		_threadName = threadName;
		start();
	}
	
	/**
	 * Returns the thread name of the scoreboard entry.
	 * @return the thread name
	 */
	public String getName() {
		return _threadName;
	}
	
	/**
	 * Returns the remote IP address of the request.
	 * @return the IP address
	 * @see ServletScoreboardEntry#setRemoteAddr(String)
	 * @see ServletScoreboardEntry#getRemoteHost()
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}
	
	/**
	 * Returns the remote host name of the requeust.
	 * @return the host name
	 * @see ServletScoreboardEntry#setRemoteHost(String)
	 * @see ServletScoreboardEntry#getRemoteAddr()
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}
	
	/**
	 * Returns the start time of the request.
	 * @return the start date/time
	 * @see ServletScoreboardEntry#getEndTime()
	 * @see ServletScoreboardEntry#getExecutionTime()
	 */
	public Instant getStartTime() {
		return Instant.ofEpochMilli(_startTime);
	}
	
	/**
	 * Returns the Servlet thread execution count.
	 * @return the execution count
	 */
	public long getCount() {
		return _execCount;
	}
	
	/**
	 * Returns the completion time of the requeust.
	 * @return the completion date/time or null if still executing
	 * @see ServletScoreboardEntry#complete()
	 * @see ServletScoreboardEntry#getStartTime()
	 */
	public Instant getEndTime() {
		return (_endTime == 0) ? null : Instant.ofEpochMilli(_endTime);
	}
	
	/**
	 * Returns the execution time of the request.
	 * @return the execution time in milliseconds
	 * @see ServletScoreboardEntry#getEndTime()
	 * @see ServletScoreboardEntry#complete()
	 */
	public long getExecutionTime() {
		return ((_endTime == 0) ? System.currentTimeMillis() : _endTime) - _startTime;
	}
	
	/**
	 * Returns the URL of the requuest.
	 * @return the URL
	 * @see ServletScoreboardEntry#setURL(String)
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns whether the thread is still alive.
	 * @return TRUE if alive, otherwise FALSE
	 * @see ServletScoreboardEntry#setAlive(boolean)
	 */
	public boolean isAlive() {
		return _isAlive;
	}
	
	/**
	 * Marks the request as complete and sets the completion time.
	 */
	public void complete() {
		_endTime = System.currentTimeMillis();
	}
	
	/**
	 * Starts the servlet timer and updates the execution count.
	 */
	public void start() {
		_startTime = System.currentTimeMillis();
		_endTime = 0;
		_execCount++;
	}
	
	/**
	 * Updates the remote IP address of the requeust.
	 * @param remoteAddr the IP address
	 * @see ServletScoreboardEntry#getRemoteAddr()
	 * @see ServletScoreboardEntry#setRemoteHost(String)
	 */
	public void setRemoteAddr(String remoteAddr) {
		_remoteAddr = remoteAddr;
	}
	
	/**
	 * Updates the remote host name of the requeust.
	 * @param remoteHost the host name
	 * @see ServletScoreboardEntry#getRemoteHost()
	 * @see ServletScoreboardEntry#setRemoteAddr(String)
	 */
	public void setRemoteHost(String remoteHost) {
		_remoteHost = remoteHost;
	}
	
	/**
	 * Updates whether the thread is still alive.
	 * @param isAlive TRUE if alive, otherwise FALSE
	 * @see ServletScoreboardEntry#isAlive()
	 */
	public void setAlive(boolean isAlive) {
		_isAlive = isAlive;
	}

	/**
	 * Sets the URL of the request.
	 * @param url the URL
	 * @see ServletScoreboardEntry#getURL()
	 */
	public void setURL(String url) {
		_url = url;
	}
	
	@Override
	public int hashCode() {
		return _threadName.hashCode();
	}
	
	/**
	 * Compares two scorebard entries by comparing their thread names.
	 */
	@Override
	public int compareTo(ServletScoreboardEntry sse2) {
		return _threadName.compareTo(sse2._threadName);
	}
	
	/**
	 * Returns the CSS class name if in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		if (_endTime > 0)
			return null;

		return (getExecutionTime() > 20000) ? "warn" : "opt2";
	}
}