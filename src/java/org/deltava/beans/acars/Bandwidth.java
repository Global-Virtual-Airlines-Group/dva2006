// Copyright 2008, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A bean to store ACARS bandwidth statistics. 
 * @author Luke
 * @version 8.0
 * @since 2.1
 */

public class Bandwidth implements Comparable<Bandwidth> {

	private final Instant _startDate;
	private int _interval = 1;
	
	private int _cons;
	private int _errors;
	private int _msgsIn;
	private int _msgsOut;
	private long _bytesIn;
	private long _bytesOut;
	private long _bytesSaved;
	
	private int _peakCons;
	private int _peakMsgs;
	private long _peakBytes;
	
	/**
	 * Initializes the bean.
	 * @param dt the start/date of this window
	 * @throws NullPointerException if dt is null
	 */
	public Bandwidth(Instant dt) {
		super();
		_startDate = dt.truncatedTo(ChronoUnit.MINUTES);
	}
	
	/**
	 * Returns the start date of the window.
	 * @return the window start date/time
	 */
	public Instant getDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end date of the window.
	 * @return the window end date/time
	 */
	public Instant getEndDate() {
		return _startDate.plusSeconds(_interval * 60);
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
	 * Returns the number of bytes saved by data compression during the window.
	 * @return the number of bytes
	 */
	public long getBytesSaved() {
		return _bytesSaved;
	}
	
	/**
	 * Returns the number of write errors during the window.
	 * @return the number of errors
	 */
	public int getErrors() {
		return _errors;
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
	 * @param cons the number of connections
	 */
	public void setConnections(int cons) {
		_cons = Math.max(0, cons);
	}
	
	/**
	 * Updates the number of errors during the window.
	 * @param errors the number of errors
	 */
	public void setErrors(int errors) {
		_errors = Math.max(0, errors);
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
	 * Updates the number of bytes saved by data compession
	 * @param bytesSaved the number of bytes
	 */
	public void setBytesSaved(long bytesSaved) {
		_bytesSaved = bytesSaved;
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
	
	@Override
	public int hashCode() {
		return _startDate.hashCode();
	}
	
	@Override
	public String toString() {
		return _startDate.toString() + "-" + getEndDate().toString();
	}
	
	@Override
	public int compareTo(Bandwidth bw2) {
		int tmpResult = _startDate.compareTo(bw2._startDate);
		return (tmpResult == 0)  ? Integer.compare(_interval, bw2._interval) : tmpResult;
	}
}