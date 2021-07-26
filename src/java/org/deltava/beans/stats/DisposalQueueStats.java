// Copyright 2012, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store Flight Report disposal queue statistics.
 * @author Luke
 * @version 10.1
 * @since 5.0
 */

public class DisposalQueueStats implements java.io.Serializable {

	private final Instant _dt;
	private final int _size;
	private final double _avgAge;
	private double _minAge;
	private double _maxAge;
	
	private Map<String, Integer> _cnts = new TreeMap<String, Integer>();
	
	/**
	 * Creates the bean.
	 * @param dt the effective date/time
	 * @param total the total number of pending Flight Reports
	 * @param avgAge the average pending time of non-held flight reports in hours
	 */
	public DisposalQueueStats(Instant dt, int total, double avgAge) {
		super();
		_dt = dt;
		_size = Math.max(0, total);
		_avgAge = Math.max(0, avgAge);
	}
	
	/**
	 * Returns the effective date.
	 * @return the effective date/time
	 */
	public Instant getDate() {
		return _dt;
	}
	
	/**
	 * Returns the total number of pending Flight Reports.
	 * @return the number of flight reports
	 */
	public int getSize() {
		return _size;
	}
	
	/**
	 * Returns the average age of non-held flight reports.
	 * @return the average age in hours
	 */
	public double getAverageAge() {
		return _avgAge;
	}
	
	/**
	 * Returns the maximum age of a non-held flight report.
	 * @return the age in hours
	 */
	public double getMaxAge() {
		return _maxAge;
	}
	
	/**
	 * Returns the minimum age of a non-held flight report.
	 * @return the age in hours
	 */
	public double getMinAge() {
		return _minAge;
	}
	
	/**
	 * Returns the average age of a non-held flight report, excluding the outliers.
	 * @return the adjusted average age in hours, or zero if less than two flight reports
	 */
	public double getAdjustedAge() {
		double totalAge = (_maxAge * _size) - _minAge - _maxAge;
		return (totalAge <= 0) ? 0 : totalAge / (_size - 2); 
	}
	
	/**
	 * Sets the minimum and maximum age of non-held flight reports.
	 * @param min the minimum age in hours
	 * @param max the maximum age in hours
	 */
	public void setMinMax(double min, double max) {
		_minAge = Math.max(0, min);
		_maxAge = Math.max(_minAge, max);
	}
	
	/**
	 * Adds an equipment-specific pending flight report count.
	 * @param eqType the equipment type
	 * @param cnt the number of pending flight reports
	 */
	public void addCount(String eqType, int cnt) {
		_cnts.put(eqType, Integer.valueOf(cnt));
	}
	
	/**
	 * Returns equipment-specific pending flight report counts.
	 * @return a Map of pending counts, keyed by equipment type
	 */
	public Map<String, Integer> getCounts() {
		return new LinkedHashMap<String, Integer>(_cnts);
	}
}