// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A bean to store System Information statistics.
 * @author Luke
 * @version 7.2
 * @since 6.4
 * @param <T> the value class 
 */

public class SystemStatistics<T extends Number> implements java.io.Serializable, Comparable<SystemStatistics<Number>> {
	
	private final String _label;
	private final T _count;

	/**
	 * Create a new Statistics bean.
	 * @param label the statistics label
	 * @param count the statistics count
	 */
	public SystemStatistics(String label, T count) {
		super();
		_label = label;
		_count = count;
	}

	/**
	 * Return the count.
	 * @return the count
	 */
	public T getCount() {
		return _count;
	}
	
	/**
	 * Return the label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	@Override
	public int hashCode() {
		return _label.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_label);
		return buf.append(':').append(String.valueOf(_count)).toString();
	}
	
	@Override
	public int compareTo(SystemStatistics<Number> s2) {
		int tmpResult = Double.valueOf(_count.doubleValue()).compareTo(Double.valueOf(s2._count.doubleValue()));
		return (tmpResult == 0) ? _label.compareTo(s2._label) : tmpResult;
	}
}