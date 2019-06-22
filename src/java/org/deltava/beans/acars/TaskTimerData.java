// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * A bean to store ACARS client task timer statistics.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class TaskTimerData implements Comparable<TaskTimerData> {
	
	private final String _name;
	private final int _tickSize;
	
	private long _count;
	private long _total;
	private int _min;
	private int _max;

	/**
	 * Creates the bean.
	 * @param name the timer name
	 * @param tickSize the number of .NET ticks per millisecond 
	 */
	public TaskTimerData(String name, int tickSize) {
		super();
		_name = name;
		_tickSize = tickSize;
	}
	
	/**
	 * Returns the timer name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the number of .NET ticks per millisecond.
	 * @return the number of ticks
	 */
	public int getTickSize() {
		return _tickSize;
	}
	
	/**
	 * Returns the average execution time.
	 * @return the average execution time in ticks, or zero if never executed
	 */
	public double getAverage() {
		return (_count == 0) ? 0 : _total * 1.0d / _count;
	}
	
	/**
	 * Returns the total execution time.
	 * @return the total time in ticks
	 */
	public long getTotal() {
		return _total;
	}
	
	/**
	 * Returns the total number of executions.
	 * @return the execution count
	 */
	public long getCount() {
		return _count;
	}
	
	/**
	 * Returns the maximum execution time.
	 * @return the execution time in ticks
	 */
	public int getMin() {
		return _min;
	}
	
	/**
	 * Returns the minimum execution time.
	 * @return the execution time in ticks
	 */
	public int getMax() {
		return _max;
	}

	/**
	 * Updates the total execution time.
	 * @param total
	 */
	public void setTotal(long total) {
		_total = Math.max(0, total);
	}
	
	/**
	 * Updates the execution count.
	 * @param cnt the times executed
	 */
	public void setCount(long cnt) {
		_count = Math.max(0, cnt);
	}
	
	/**
	 * Updates the minimum execution time.
	 * @param m the minimum value
	 */
	public void setMin(int m) {
		_min = Math.max(0, m);
	}
	
	/**
	 * Updates the maximum execution time.
	 * @param m the maximum value
	 */
	public void setMax(int m) {
		_max = Math.max(_min, m);
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public int compareTo(TaskTimerData tt2) {
		return _name.compareTo(tt2._name);
	}
}