// Copyright 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.*;

/**
 * A bean to track performance metrics data.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class PerformanceMetrics implements java.io.Serializable, AuthoredBean, Comparable<PerformanceMetrics> {

	private String _name;
	private double _avg;
	private double _max;
	private double _min;
	private long _count;
	
	private int _authorID;
	
	/**
	 * Creates a new performance metrics bean.
	 * @param catName the category name
	 * @throws NullPointerException if catName is null
	 * @see PerformanceMetrics#getName()
	 */
	public PerformanceMetrics(String catName) {
		super();
		setName(catName);
	}
	
	/**
	 * Returns the category name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the category count.
	 * @return the count
	 */
	public long getCount() {
		return _count;
	}
	
	/**
	 * Returns the average.
	 * @return the average
	 */
	public double getAverage() {
		return _avg;
	}
	
	/**
	 * Returns the maximum value.
	 * @return the maximum
	 */
	public double getMaximum() {
		return _max;
	}
	
	/**
	 * Returns the minimum value.
	 * @return the minimum
	 */
	public double getMinimum() {
		return _min;
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}

	/**
	 * Updates the category name.
	 * @param name the category name
	 * @throws NullPointerException if name is null
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the average value.
	 * @param avg the average
	 */
	public void setAverage(double avg) {
		_avg = avg;
	}
	
	/**
	 * Updates the category count.
	 * @param count the count
	 */
	public void setCount(long count) {
		_count = count;
	}
	
	/**
	 * Updates the data series limits.
	 * @param mn the minimum
	 * @param mx the maximum
	 */
	public void setLimits(double mn, double mx) {
		_max = mx;
		_min = mn;
	}
	
	@Override
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Compares two entries by comparing their category names.
	 */
	@Override
	public int compareTo(PerformanceMetrics m2) {
		return _name.compareTo(m2._name);
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}
}