// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.DatabaseBean;

/**
 * A bean used to track average landing speeds.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class LandingStatistics extends DatabaseBean {
	
	private String _name;
	private String _eqType;
	
	private int _legs;
	private double _hours;
	
	private double _vSpeed;
	private double _stdDev;

	/**
	 * Initializes the bean. When displaying statistics across multiple pilots or
	 * equipment types, either parameter (but not both) can be null
	 * @param name the pilot name
	 * @param eqType the equipment type
	 */
	public LandingStatistics(String name, String eqType) {
		super();
		_name = name;
		_eqType = eqType;
	}

	/**
	 * Returns the pilot name.
	 * @return the pilot name, or null
	 */
	public String getPilotName() {
		return _name;
	}
	
	/**
	 * Returns the equipment type.
	 * @return the equipment type, or null
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns the number of legs flown.
	 * @return the number of legs
	 */
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Returns the number of hours logged.
	 * @return the number of hours
	 */
	public double getHours() {
		return _hours;
	}
	
	/**
	 * Returns the average touchdown speed.
	 * @return the average speed in feet per minute
	 */
	public double getAverageSpeed() {
		return _vSpeed;
	}

	/**
	 * Returns the standard deviation of touchdown speeds.
	 * @return the standard deviation in feet per minute
	 */
	public double getStdDeviation() {
		return _stdDev;
	}
	
	/**
	 * Updates the number of legs flown.
	 * @param legs the number of legs
	 */
	public void setLegs(int legs) {
		_legs = Math.max(0, legs);
	}
	
	/**
	 * Updates the number of hours logged.
	 * @param hours the number of hours 
	 */
	public void setHours(double hours) {
		_hours = Math.max(0, hours);
	}

	/**
	 * Updates the average touchdown speed.
	 * @param spd the average speed in feet per minute.
	 */
	public void setAverageSpeed(double spd) {
		_vSpeed = Math.min(0.001, spd); 
	}
	
	/**
	 * Updates the standard deviation of touchdown speeds.
	 * @param sd the standard deviation in feet per minute.
	 */
	public void setStdDeviation(double sd) {
		_stdDev = Math.max(0.001, sd);
	}
	
	/**
	 * Compares two beans by comparing their average touchdown speeds.
	 */
	public int compareTo(Object o) {
		LandingStatistics ls2 = (LandingStatistics) o;
		return new Double(_vSpeed).compareTo(new Double(ls2._vSpeed));
	}
}