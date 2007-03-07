// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.util.Date;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store servlet scoreboard entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServletScoreboardEntry implements java.io.Serializable, Comparable, ViewEntry {

	private String _threadName;
	private String _remoteAddr;
	private String _remoteHost;
	
	private long _startTime;
	private long _endTime;
	private long _execCount;
	
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
	public Date getStartTime() {
		return new Date(_startTime);
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
	public Date getEndTime() {
		return (_endTime == 0) ? null : new Date(_endTime);
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
	 * Sets the URL of the request.
	 * @param url the URL
	 * @see ServletScoreboardEntry#getURL()
	 */
	public void setURL(String url) {
		_url = url;
	}
	
	/**
	 * Compares two scorebard entries by comparing their thread names.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		ServletScoreboardEntry sse2 = (ServletScoreboardEntry) o;
		return _threadName.compareTo(sse2._threadName);
	}
	
	/**
	 * Returns the CSS class name if in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		if (_endTime > 0)
			return null;

		return (getExecutionTime() > 20000) ? "warn" : "opt2";
	}
}