// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

/**
 * A bean to store ACARS bandwidth statistics. 
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class Bandwidth implements Comparable<Bandwidth> {

	private Date _startDate;
	private int _interval = 1;
	
	private int _cons; 
	private int _msgsIn;
	private int _msgsOut;
	private long _bytesIn;
	private long _bytesOut;
	
	private int _peakCons;
	private int _peakMsgs;
	private long _peakBytes;
	
	/**
	 * Initializes the bean.
	 * @param dt the start/date of this window
	 * @throws NullPointerException if dt is null
	 */
	public Bandwidth(Date dt) {
		super();
		Calendar cld = Calendar.getInstance();
		cld.setTime(dt);
		cld.set(Calendar.SECOND, 0);
		cld.set(Calendar.MILLISECOND, 0);
		_startDate = cld.getTime();
	}
	
	/**
	 * Returns the start date of the window.
	 * @return the window start date/time
	 */
	public Date getDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end date of the window.
	 * @return the window end date/time
	 */
	public Date getEndDate() {
		return new Date(_startDate.getTime() + (_interval * 60000));
	}
	
	/**
	 * Returns the window size.
	 * @return the window size in minutes
	 */
	public int getInterval() {
		return _interval;
	}
	
	/**
	 * Returns the number of connections during the window.
	 * @return the number of connections
	 */
	public int getConnections() {
		return _cons;
	}
	
	/**
	 * Returns the number of inbound messages during the window.
	 * @return the number of messages
	 */
	public int getMsgsIn() {
		return _msgsIn;
	}
	
	/**
	 * Returns the number of outbound messages during the window.
	 * @return the number of messages
	 */
	public int getMsgsOut() {
		return _msgsOut;
	}
	
	/**
	 * Returns the number of inbound bytes during the window.
	 * @return the number of bytes
	 */
	public long getBytesIn() {
		return _bytesIn;
	}
	
	/**
	 * Returns the number of outbound bytes during the window.
	 * @return the number of bytes
	 */
	public long getBytesOut() {
		return _bytesOut;
	}
	
	/**
	 * Returns the maximum number of connections for the period.
	 * @return the number of connections
	 */
	public int getMaxConnections() {
		return Math.max(_peakCons, _cons);
	}
	
	/**
	 * Returns the maximum number of messages for the period.
	 * @return the number of messages
	 */
	public int getMaxMsgs() {
		return Math.max(_peakMsgs, _msgsIn + _msgsOut);
	}
	
	/**
	 * Returns the maximum number of bytes transferred for the period.
	 * @return the number of bytes
	 */
	public long getMaxBytes() {
		return Math.max(_peakBytes, _bytesIn + _bytesOut);
	}
	
	/**
	 * Updates the number of connections during the window.
	 * @param cons the number of connections.
	 */
	public void setConnections(int cons) {
		_cons = Math.max(0, cons);
	}
	
	/**
	 * Updates the number of messages during the window.
	 * @param msgIn the number of inbound messages
	 * @param msgOut the number of outbound messages
	 */
	public void setMessages(int msgIn, int msgOut) {
		_msgsIn = Math.max(0, msgIn);
		_msgsOut = Math.max(0, msgOut);
	}
	
	/**
	 * Updates the bandwidth during the window.
	 * @param bytesIn the number of bytes in
	 * @param bytesOut the number of bytes out
	 */
	public void setBytes(long bytesIn, long bytesOut) {
		_bytesIn = Math.max(0, bytesIn);
		_bytesOut = Math.max(0, bytesOut);
	}
	
	/**
	 * Updates the maximum number of connections for the period. <i>This is only populated
	 * if the window is greater than one minute.</i>
	 * @param cons the maximum number of connections
	 */
	public void setMaxConnections(int cons) {
		_peakCons = Math.max(0, cons);
	}
	
	/**
	 * Updates the maximum number of messages for the period. <i>This is only populated
	 * if the window is greater than one minute.</i>
	 * @param msgs the maximum number of messages
	 */
	public void setMaxMsgs(int msgs) {
		_peakMsgs = Math.max(0, msgs);
	}
	
	/**
	 * Updates the maximum number of bytes transferred for the period. <i>This is only populated
	 * if the window is greater than one minute.</i>
	 * @param bytes the maximum number of bytes
	 */
	public void setMaxBytes(long bytes) {
		_peakBytes = Math.max(0, bytes);
	}
	
	/**
	 * Sets the window size.
	 * @param interval the size in minutes
	 */
	public void setInterval(int interval) {
		_interval = Math.max(1, interval);
	}
	
	public int hashCode() {
		return _startDate.hashCode();
	}
	
	public String toString() {
		return _startDate.toString() + "-" + getEndDate().toString();
	}
	
	/**
	 * Compares two beans by comparing their start dates and intervals.
	 */
	public int compareTo(Bandwidth bw2) {
		int tmpResult = _startDate.compareTo(bw2._startDate);
		return (tmpResult == 0)  ? Integer.valueOf(_interval).compareTo(Integer.valueOf(bw2._interval)) : tmpResult;
	}
}