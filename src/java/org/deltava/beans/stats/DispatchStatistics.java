// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to track Dispatcher statistics. 
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class DispatchStatistics extends DatabaseBean {

	private double _hours;
	private int _legs;
	
	/**
	 * Creates the bean.
	 * @param dispatcherID the Dispatcher dataabase ID
	 */
	public DispatchStatistics(int dispatcherID) {
		super();
		setID(dispatcherID);
	}
	
	/**
	 * Returns the time spent providing Dispatch services.
	 * @return the time in hours
	 */
	public double getHours() {
		return _hours;
	}
	
	/**
	 * Returns the number of flights dispatched.
	 * @return the number of flights
	 */
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Updates the time spent providing Dispatch services.
	 * @param hrs the time spent in hours
	 */
	public void setHours(double hrs) {
		_hours = hrs;
	}
	
	/**
	 * Updates the number of flights dispatched.
	 * @param legs the number of flights
	 */
	public void setLegs(int legs) {
		_legs = legs;
	}
}