// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A bean to track flight load statistics. 
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class LoadStatistics implements Comparable<LoadStatistics> {
	
	private final String _label;
	private double _load;
	private int _pax;

	/**
	 * Creates the bean.
	 * @param label the view label 
	 */
	public LoadStatistics(String label) {
		super();
		_label = label;
	}

	/**
	 * Returns the view label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the average load factor.
	 * @return the load factor from 0 to 1
	 */
	public double getLoadFactor() {
		return _load;
	}
	
	/**
	 * Returns the total number of passengers carried.
	 * @return the number of passengers
	 */
	public int getPax() {
		return _pax;
	}
	
	/**
	 * Updates the average load factor.
	 * @param lf the load factor
	 */
	public void setLoad(double lf) {
		_load = lf;
	}
	
	/**
	 * Updates the total number of passengers carried. 
	 * @param pax the number of passengers
	 */
	public void setPax(int pax) {
		_pax = pax;
	}

	@Override
	public int hashCode() {
		return _label.hashCode();
	}

	@Override
	public int compareTo(LoadStatistics ls2) {
		return Double.compare(_load, ls2._load);
	}
}