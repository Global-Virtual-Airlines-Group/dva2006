// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.LocalDate;

/**
 * A bean to store yearly totals with rollover amounts.
 * @author Luke
 * @version 11.2
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
	
	/**
	 * Converts a YearlyTotal bean to one supporting rollover.
	 * @param yt a YearlyTotal bean
	 */
	public RolloverYearlyTotal(YearlyTotal yt) {
		super(yt.getYear(), yt.getID());
		addLegs(yt.getLegs(), yt.getDistance(), yt.getPoints());
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
	
	/**
	 * Projects year to date totals over a full year. This will have no effect on totals for a prior year. This calls the superclass
	 * to extrapolate earned totals across the year, and then adds the previous year's rollover totals to reflect the fact that they
	 * are one-time totals, not to be extrapolated across the entire year.
	 * @param ld the date to project from
	 * @return a YearlyTotal bean
	 */
	@Override
	public YearlyTotal adjust(LocalDate ld) {
		YearlyTotal yt = super.adjust(ld);
		if (yt == this) return yt;
		
		// Add rollovers
		RolloverYearlyTotal rt = new RolloverYearlyTotal(yt.getYear(), getID());
		rt.addLegs(yt.getLegs(), yt.getDistance(), yt.getPoints());
		rt.addRollover(_legs, _distance, _pts);
		return rt;
	}

	/**
	 * Merges a yearly total into this bean.
	 * @param yt a YearlyTotal bean
	 */
	public void merge(YearlyTotal yt) {
		if (yt instanceof RolloverYearlyTotal rt) {
			addRollover(rt._legs, rt._distance, rt._pts);
			addLegs(rt.getLegs() - rt._legs, rt.getDistance() - rt._distance, rt.getPoints() - rt._pts);
		} else
			addLegs(yt.getLegs(), yt.getDistance(), yt.getPoints());
	}
	
	@Override
	public void reset(int year) {
		super.reset(year);
		_legs = 0;
		_distance = 0;
		_pts = 0;
	}
}