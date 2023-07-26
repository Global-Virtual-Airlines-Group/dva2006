// Copyright 2012, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.concurrent.TimeUnit;

/**
 * A utility class to time operations. 
 * @author Luke
 * @version 11.1
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
		return TimeUnit.MILLISECONDS.convert(_interval, TimeUnit.NANOSECONDS);
	}
	
	/**
	 * Returns the currently elapsed time without stopping the timer.
	 * @return the execution time in nanoseconds
	 */
	public long getInterval() {
		return System.nanoTime() - _start;
	}
	
	/**
	 * Returns the execution time in nanoseconds.
	 * @return the execution time
	 */
	public long getNanos() {
		return _interval;
	}
	
	/**
	 * Returns the execution time in milliseconds.
	 * @return the execution time
	 */
	public long getMillis() {
		return TimeUnit.MILLISECONDS.convert(_interval, TimeUnit.NANOSECONDS);
	}
}