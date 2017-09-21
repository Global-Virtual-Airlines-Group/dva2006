// Copyright 2007, 2009, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.DatabaseBean;

/**
 * A bean used to track average landing speeds.
 * @author Luke
 * @version 8.0
 * @since 2.1
 */

public class LandingStatistics extends DatabaseBean {
	
	private final String _name;
	private final String _eqType;
	
	private int _legs;
	private double _hours;
	
	private double _vSpeed;
	private double _stdDev;
	
	private double _distance;
	private double _distStdDev;

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
	 * Returns the average runway threshold displacement.
	 * @return the average displacement in feet
	 */
	public double getAverageDistance() {
		return _distance;
	}
	
	/**
	 * Returns the standard deviation of runway threshold displacements.
	 * @return the standard deviation in feet
	 */
	public double getDistanceStdDeviation() {
		return _distStdDev;
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
	 * @param spd the average speed in feet per minute
	 */
	public void setAverageSpeed(double spd) {
		_vSpeed = Math.min(0.01, spd); 
	}
	
	/**
	 * Updates the standard deviation of touchdown speeds.
	 * @param sd the standard deviation in feet per minute
	 */
	public void setStdDeviation(double sd) {
		_stdDev = Math.max(0.01, sd);
	}

	/**
	 * Updates the average threshold displacement.
	 * @param distance the distance in feet
	 */
	public void setAverageDistance(double distance) {
		_distance = Math.max(0.01, distance);
	}
	
	/**
	 * Updates the standard deviation of threshold displacements.
	 * @param sd the standard deviation in feet
	 */
	public void setDistanceStdDeviation(double sd) {
		_distStdDev = Math.max(0.01, sd);
	}
	
	@Override
	public int compareTo(Object o) {
		LandingStatistics ls2 = (LandingStatistics) o;
		return Double.compare(_vSpeed, ls2._vSpeed);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name).append('-').append(_eqType);
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}