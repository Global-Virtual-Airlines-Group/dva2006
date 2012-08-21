// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A utility class to time operations. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class TaskTimer {
	
	private long _start;
	private long _end;
	private long _interval;
	
	/**
	 * Creates and starts a timer.
	 */
	public TaskTimer() {
		start();
	}

	/**
	 * Starts the timer.
	 */
	public void start() {
		_end = 0; _interval = 0;
		_start = System.nanoTime();
	}
	
	/**
	 * Stops the timer.
	 * @return the execution time in milliseconds
	 */
	public long stop() {
		_end = System.nanoTime();
		_interval = (_end - _start);
		return (_interval / 1000000);
	}
	
	/**
	 * Returns the execution time in nanoseconds.
	 * @return the execution time
	 */
	public long getNanos() {
		return _interval;
	}
}