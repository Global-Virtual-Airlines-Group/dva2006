// Copyright 2020, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;
import java.time.*;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store yearly Pilot Elite status totals.
 * @author Luke
 * @version 11.1
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
	 * Adds a Flight's totals.
	 * @param sc a FlightEliteScore
	 */
	public void add(FlightEliteScore sc) {
		_pts += sc.getPoints();
		if (!sc.getScoreOnly()) {
			_legs += sc.getLegs();
			_distance += sc.getDistance();
		}
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
	 * Projects year to date totals over a full year. This will have no effect on totals for a prior year.
	 * @param ld the date to project from
	 * @return a YearlyTotal bean
	 */
	public YearlyTotal adjust(LocalDate ld) {
		if (ld.getYear() != _year) return this;
		int daysInYear = ld.lengthOfYear();
		float factor =  1 / (ld.getDayOfYear() * 1.0f / daysInYear);
		YearlyTotal yt = new YearlyTotal(_year, getID());
		yt.addLegs(Math.round(_legs * factor), Math.round(_distance * factor), Math.round(_pts * factor));
		return yt;
	}
	
	/**
	 * Returns whether these totals match the requirements for a given Elite level.
	 * @param el the EliteLevel
	 * @return TRUE if the legs, distance and points all exceed the requirements
	 */
	public boolean matches(EliteLevel el) {
		if (el == null) return false;
		return ((el.getLegs() > 0) && (_legs >= el.getLegs())) || ((el.getDistance() > 0) && (_distance >= el.getDistance())) || ((el.getPoints() > 0) && (_pts >= el.getPoints()));
	}
	
	/**
	 * Returns the highest level whose requirements match this total.
	 * @param lvls a Collection of EliteLevels
	 * @return an EliteLevel, or the first level if none match
	 */
	public EliteLevel matches(Collection<EliteLevel> lvls) {
		TreeSet<EliteLevel> levels = new TreeSet<EliteLevel>(lvls);
		return levels.descendingSet().stream().filter(this::matches).findFirst().orElse(levels.first());
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
	 * @param sc the FlightEliteScore for that leg
	 * @return an UpgradeReason
	 */
	public UpgradeReason wouldMatch(EliteLevel el, FlightEliteScore sc) {
		if ((el == null) || matches(el))
			return UpgradeReason.NONE;
		
		if (!sc.getScoreOnly() && ((_legs + 1) >= el.getLegs()) && (_legs < el.getLegs())) 
			return UpgradeReason.LEGS;
		if (!sc.getScoreOnly() && ((_distance + sc.getDistance()) >= el.getDistance()) && (_distance < el.getDistance()))
			return UpgradeReason.DISTANCE;
		if ((_pts + sc.getPoints()) >= el.getPoints() && (_pts < el.getPoints()) && (el.getPoints() > 0))
			return UpgradeReason.UNITS;

		return UpgradeReason.NONE;
	}
	
	@Override
	public String toString() {
		return "[" + _legs + "/" + _distance + "/" + _pts + "]";
	}
}