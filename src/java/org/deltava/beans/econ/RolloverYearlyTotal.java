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
	
	private int _roLegs;
	private int _roDistance;
	private int _roPts;

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
		return super.getLegs() + _roLegs;
	}
	
	@Override
	public int getDistance() {
		return super.getDistance() + _roDistance;
	}
	
	@Override
	public int getPoints() {
		return super.getPoints() + _roPts;
	}
	
	/**
	 * Returns rollover totals for the year.
	 * @return a YearlyTotal bean
	 */
	public YearlyTotal getRollover() {
		YearlyTotal yt = new YearlyTotal(getYear(), getID());
		yt.addLegs(_roLegs, _roDistance, _roPts);
		return yt;
	}

	/**
	 * Adds rollover totals.
	 * @param legs the number of legs
	 * @param distance the distance
	 * @param pts the points
	 */
	public void addRollover(int legs, int distance, int pts) {
		_roLegs += legs;
		_roDistance += distance;
		_roPts += pts;
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
		rt.addRollover(_roLegs, _roDistance, _roPts);
		return rt;
	}

	/**
	 * Merges a yearly total into this bean.
	 * @param yt a YearlyTotal bean
	 */
	public void merge(YearlyTotal yt) {
		if (yt instanceof RolloverYearlyTotal rt) {
			addRollover(rt._roLegs, rt._roDistance, rt._roPts);
			addLegs(rt.getLegs() - rt._roLegs, rt.getDistance() - rt._roDistance, rt.getPoints() - rt._roPts);
		} else
			addLegs(yt.getLegs(), yt.getDistance(), yt.getPoints());
	}
	
	@Override
	public void reset(int year) {
		super.reset(year);
		_roLegs = 0;
		_roDistance = 0;
		_roPts = 0;
	}
}