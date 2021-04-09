// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.econ.*;

/**
 * A bean to store elite program statistics.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteStats implements EliteTotals, EliteLevelBean, Comparable<EliteStats> {
	
	private EliteLevel _lvl;
	private int _pilots;
	
	private int _legs;
	private int _distance;
	private int _pts;
	
	private int _maxLegs;
	private int _maxDistance;
	
	private double _sdLegs;
	private double _sdDistance;

	/**
	 * Creates the bean.
	 * @param lvl the EliteLevel
	 */
	public EliteStats(EliteLevel lvl) {
		super();
		_lvl = lvl;
	}
	
	@Override
	public EliteLevel getLevel() {
		return _lvl;
	}
	
	/**
	 * Returns the total numebr of Pilots at this Elite status level.
	 * @return the number of pilots
	 */
	public int getPilots() {
		return _pilots;
	}
	
	@Override
	public int getLegs() {
		return _legs;
	}
	
	@Override
	public int getDistance() {
		return _distance;
	}
	
	@Override
	public int getPoints() {
		return _pts;
	}
	
	/**
	 * Returns the maximum number of flight legs flown by a pilot with this status.
	 * @return the number of legs
	 */
	public int getMaxLegs() {
		return _maxLegs;
	}
	
	/**
	 * Returns the maximum distance flown by a pilot with this status.
	 * @return the distance in miles
	 */
	public int getMaxDistance() {
		return _maxDistance;
	}
	
	/**
	 * Returns the standard deviation of flight legs flown by Pilots at this Elite status level.
	 * @return the standard deviation in legs
	 */
	public double getLegSD() {
		return _sdLegs;
	}
	
	/**
	 * Returns the standard deviation of distance flown by Pilots at this Elite status level.
	 * @return the standard deviation in miles
	 */
	public double getDistanceSD() {
		return _sdDistance;
	}
	
	public void add(int pilots, int legs, int distance, int pts) {
		_pilots += pilots;
		_legs += legs;
		_distance += distance;
		_pts += pts;
	}

	/**
	 * Updates the total number of Pilots at this Elite status level.
	 * @param cnt the number of pilots
	 */
	public void setPoilots(int cnt) {
		_pilots = cnt;
	}
	
	/**
	 * Updates the total number of flight legs flown by Pilots at this Elite status level.
	 * @param cnt the number of legs
	 */
	public void setLegs(int cnt) {
		_legs = cnt;
	}
	
	/**
	 * Updates the total distance flown by Pilots at this Elite status level.
	 * @param dst the distance in miles
	 */
	public void setDistance(int dst) {
		_distance = dst;
	}
	
	/**
	 * Updates the total flight score obtained by Pilots at this Elite status level.
	 * @param pts the number of points
	 */
	public void setPoints(int pts) {
		_pts = pts;
	}
	
	/**
	 * Updates the maximum number of flight legs flown by a pilot with this status.
	 * @param cnt the number of legs
	 */
	public void setMaxLegs(int cnt) {
		_maxLegs = cnt;
	}
	
	/**
	 * Returns the maximum distance flown by a pilot with this status.
	 * @param dst the distance in miles
	 */
	public void setMaxDistance(int dst) {
		_maxDistance = dst;
	}
	
	/**
	 * Updates the standard deviation of flight legs and distance at this Elite status level.
	 * @param legs the standard deviation number of legs
	 * @param dst the standard deviation distance in miles
	 */
	public void setStandardDeviation(double legs, double dst) {
		_sdLegs = legs;
		_sdDistance = dst;
	}
	
	@Override
	public void setLevel(EliteLevel lvl) {
		_lvl = lvl;
	}

	@Override
	public int compareTo(EliteStats es2) {
		int tmpResult = EliteTotals.compare(_lvl, es2._lvl);
		return (tmpResult == 0) ? Integer.compare(_legs, es2._legs) : tmpResult;
	}
}