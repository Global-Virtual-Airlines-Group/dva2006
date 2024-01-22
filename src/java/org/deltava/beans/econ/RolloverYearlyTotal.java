// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * A bean to store yearly totals with rollover amounts.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class RolloverYearlyTotal extends YearlyTotal {
	
	private int _legs;
	private int _distance;
	private int _pts;

	/**
	 * Creates the bean
	 * @param year the program year
	 * @param pilotID the Pilot's Database ID
	 */
	public RolloverYearlyTotal(int year, int pilotID) {
		super(year, pilotID);
	}

	@Override
	public int getLegs() {
		return super.getLegs() + _legs;
	}
	
	@Override
	public int getDistance() {
		return super.getDistance() + _distance;
	}
	
	@Override
	public int getPoints() {
		return super.getPoints() + _pts;
	}
	
	/**
	 * Returns rollover totals for the year.
	 * @return a YearlyTotal bean
	 */
	public YearlyTotal getRollover() {
		YearlyTotal yt = new YearlyTotal(getYear(), getID());
		yt.addLegs(_legs, _distance, _pts);
		return yt;
	}

	/**
	 * Adds rollover totals.
	 * @param legs the number of legs
	 * @param distance the distance
	 * @param pts the points
	 */
	public void addRollover(int legs, int distance, int pts) {
		_legs += legs;
		_distance += distance;
		_pts += pts;
	}
	
	@Override
	public void reset(int year) {
		super.reset(year);
		_legs = 0;
		_distance = 0;
		_pts = 0;
	}
}