// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store yearly Pilot Elite status totals.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class YearlyTotal extends DatabaseBean implements EliteTotals, Cloneable {
	
	private int _year;
	private int _legs;
	private int _distance;
	private int _pts;

	/**
	 * Creates the bean.
	 * @param year the program year;
	 * @param pilotID the Pilot's Database ID
	 */
	public YearlyTotal(int year, int pilotID) {
		super();
		setID(pilotID);
		reset(year);
	}
	
	/**
	 * Returns the accumulated legs for the year.
	 * @return the number of legs
	 */
	@Override
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Returns the accumulated distance for the year.
	 * @return the distance in miles
	 */
	@Override
	public int getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the accumulated points for the year.
	 * @return the number of points
	 */
	@Override
	public int getPoints() {
		return _pts;
	}
	
	/**
	 * Returns the program year.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Resets the totals.
	 * @param year the new program year
	 */
	public void reset(int year) {
		_year = year;
		_legs = 0;
		_distance = 0;
		_pts = 0;
	}
	
	/**
	 * Adds aggregated totals.
	 * @param legs the number of flight legs
	 * @param distance the distance in miles
	 * @param pts the flight score
	 */
	public void addLegs(int legs, int distance, int pts) {
		_legs += legs;
		_distance += distance;
		_pts += pts;
	}
	
	/**
	 * Returns whether these totals match the requirements for a given Elite level.
	 * @param el the EliteLevel
	 * @return TRUE if the legs, distance and points all exceed the requirements
	 */
	public boolean matches(EliteLevel el) {
		if (el == null) return false;
		boolean isMatch = ((el.getLegs() > 0) && (_legs >= el.getLegs()));
		isMatch |= ((el.getDistance() > 0) && (_distance >= el.getDistance()));
		return isMatch | ((el.getPoints() > 0) && (_pts >= el.getPoints()));
	}
	
	/**
	 * Returns the prerequisites required to achieve a particular Elite level.
	 * @param el the EliteLevel
	 * @return a YearlyTotal with the amounts required
	 */
	public YearlyTotal delta(EliteLevel el) {
		YearlyTotal result = new YearlyTotal(_year, getID());
		result._legs = Math.max(0, el.getLegs() - _legs);
		result._distance = Math.max(0,  el.getDistance() - _distance);
		result._pts = Math.max(0,  el.getPoints() - _pts);
		return result;
	}
	
	/**
	 * Returns whether a specific flight leg would trigger a given Elite Level.
	 * @param el the EliteLevel
	 * @param distance the leg distance in miles
	 * @param pts the flight score
	 * @return an UpgradeReason
	 */
	public UpgradeReason wouldMatch(EliteLevel el, int distance, int pts) {
		if ((el == null) || matches(el))
			return UpgradeReason.NONE;

		if ((_legs + 1) >= el.getLegs() && (_legs < el.getLegs())) 
			return UpgradeReason.LEGS;
		if ((_distance + distance) >= el.getDistance() && (_distance < el.getDistance()))
			return UpgradeReason.DISTANCE;
		if ((_pts + pts) >= el.getPoints() && (_pts < el.getPoints()) && (el.getPoints() > 0))
			return UpgradeReason.POINTS;
		
		return UpgradeReason.NONE;
	}
	
	@Override
	public String toString() {
		return "[" + _legs + "/" + _distance + "/" + _pts + "]";
	}
}