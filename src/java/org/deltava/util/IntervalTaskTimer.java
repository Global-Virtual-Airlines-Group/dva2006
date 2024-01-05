// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import static java.util.concurrent.TimeUnit.*;

import java.util.*;

/**
 * A Task Timer that allows multiple intervals to be recorded for different steps in an operation.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class IntervalTaskTimer extends TaskTimer {
	
	private final Map<String, Long> _intervals = new LinkedHashMap<String, Long>();

	/**
	 * Creates and starts the timer. 
	 */
	public IntervalTaskTimer() {
		super(false);
		start();
	}
	
	/**
	 * Starts the timer. This will also create a marker called &quot;start&quot; with the start time.
	 */
	@Override
	public void start() {
		super.start();
		_intervals.clear();
		_intervals.put("start", Long.valueOf(getStart()));
	}
	
	/**
	 * End the timer. This will also create a marker called &quot;end&quot; with the end time.
	 */
	@Override
	public long stop() {
		long execTime = super.stop();
		_intervals.put("end", Long.valueOf(getEnd()));
		return execTime;
	}

	/**
	 * Marks an interval without stopping the timer. If a marker name is reused, the previous value will be overwritten.
	 * @param name the interval name
	 * @return the current execution time, in nanoseconds
	 */
	public long mark(String name) {
		long now = System.nanoTime();
		_intervals.put(name, Long.valueOf(now));
		return now - getStart();
	}
	
	/**
	 * Returns the marker names.
	 * @return a Collection of marker names
	 */
	public Collection<String> getMarkerNames() {
		return _intervals.keySet();
	}
	
	/**
	 * Returns the exectution time for a given marker.
	 * @param name the marker name
	 * @return the execution time in milliseconds, or -1 if the marker does not exist
	 */
	public long getInterval(String name) {
		Long mrk = _intervals.get(name);
		if (mrk == null) return -1;
		
		long execTime = mrk.longValue() - getStart();
		return NANOSECONDS.toMillis(execTime); 
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(" [");
		buf.append(getMillis());
		buf.append("ms - ");
		for (Iterator<Map.Entry<String, Long>> i = _intervals.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, Long> me = i.next();
			buf.append(me.getKey());
			buf.append('=');
			buf.append(me.getValue().longValue());
			buf.append("ms");
			if (i.hasNext())
				buf.append(',');
		}
		
		buf.append(" ]");
		return buf.toString();
	}
}