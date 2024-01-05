// Copyright 2012, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import static java.util.concurrent.TimeUnit.*;

/**
 * A utility class to time operations. 
 * @author Luke
 * @version 11.1
 * @since 5.0
 */

public class TaskTimer {
	
	protected long _start;
	private long _end;
	private long _interval;

	/**
	 * Creates an unstarted timer.
	 */
	public TaskTimer() {
		this(true);
	}
	
	/**
	 * Creates and optionally starts a timer.
	 * @param doStart TRUE to start the timer, otherwise FALSE
	 */
	public TaskTimer(boolean doStart) {
		super();
		if (doStart)
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
	 * Returns the start time.
	 * @return the start time, in nanoseconds
	 */
	protected long getStart() {
		return _start;
	}
	
	/**
	 * Returns the end time.
	 * @return the end time, in nanoseconds or zero if running
	 */
	protected long getEnd() {
		return _end;
	}
	
	/**
	 * Stops the timer.
	 * @return the execution time in milliseconds
	 */
	public long stop() {
		_end = System.nanoTime();
		_interval = (_end - _start);
		return MILLISECONDS.convert(_interval, NANOSECONDS);
	}
	
	/**
	 * Returns the currently elapsed time without stopping the timer, if running.
	 * @return the execution time in nanoseconds
	 */
	public long getInterval() {
		return isRunning() ? System.nanoTime() - _start : _interval;
	}
	
	/**
	 * Returns the execution time in milliseconds.
	 * @return the execution time
	 */
	public long getMillis() {
		return MILLISECONDS.convert(getInterval(), NANOSECONDS);
	}
	
	/**
	 * Returns if the timer is currently running.
	 * @return TRUE if running, otherwise FALSE
	 */
	public boolean isRunning() {
		return (_end == 0);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("[ ");
		buf.append(getMillis());
		buf.append("ms ]");
		return buf.toString();
	}
}