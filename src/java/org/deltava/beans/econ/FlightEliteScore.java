// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store Flight Report Elite point data.
 * @author Luke
 * @version 11.1
 * @since 9.2
 */

public class FlightEliteScore extends DatabaseBean implements AuthoredBean, EliteTotals {
	
	private int _pilotID;
	private String _level;
	private int _year;
	private int _distance;
	private boolean _scoreOnly;
	
	private final Collection<EliteScoreEntry> _entries = new TreeSet<EliteScoreEntry>();

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 */
	public FlightEliteScore(int id) {
		super();
		setID(id);
	}
	
	/**
	 * Returns the Pilot's Elite status level at the time of this flight.
	 * @return the EliteLevel name
	 */
	public String getEliteLevel() {
		return _level;
	}
	
	/**
	 * Returns the Pilot's Elite status year at the time of this flight.
	 * @return the EliteLevel year
	 */
	public int getYear() {
		return _year;
	}
	
	@Override
	public final int getLegs() {
		return 1;
	}
	
	/**
	 * Returns the great circle distance of this flight.
	 * @return the distance in miles
	 */
	@Override
	public int getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the total Elite points for this flight.
	 * @return the total number of points
	 */
	@Override
	public int getPoints() {
		return _entries.stream().mapToInt(EliteScoreEntry::getPoints).sum();
	}
	
	/**
	 * Return the bonus Elite points for this flight.
	 * @return the number of bonus points
	 */
	public int getBonus() {
		return _entries.stream().filter(EliteScoreEntry::isBonus).mapToInt(EliteScoreEntry::getPoints).sum();
	}
	
	/**
	 * Returns the point entries for this flight.
	 * @return a Collection of EliteScoreEntry beans
	 */
	public Collection<EliteScoreEntry> getEntries() {
		return _entries;
	}
	
	/**
	 * Returns whether to ignore flight leg and distance for aggregation purposes. 
	 * @return TRUE to ignore leg/distance, otherwise FALSE
	 */
	public boolean getScoreOnly() {
		return _scoreOnly;
	}
	
	@Override
	public int getAuthorID() {
		return _pilotID;
	}
	
	/**
	 * Updates the Pilot's Elite level at the time of this flight.
	 * @param levelName the EliteLevel name
	 * @param year the EliteLevel year
	 */
	public void setEliteLevel(String levelName, int year) {
		_level = levelName;
		_year = year;
	}

	/**
	 * Updates the great circle distance of this flight.
	 * @param dst the distance in miles
	 */
	public void setDistance(int dst) {
		_distance = dst;
	}
	
	/**
	 * Updates whether to ignore flight leg and distance for aggregation purpose.
	 * @param scoreOnly TRUE to only count score, otherwise FALSE
	 */
	public void setScoreOnly(boolean scoreOnly) {
		_scoreOnly = scoreOnly;
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}
	
	/**
	 * Adds a score entry.
	 * @param amt the number of points
	 * @param msg the entry message
	 * @param isBonus TRUE if bonus points, otherwise FALSE
	 */
	public synchronized void add(int amt, String msg, boolean isBonus) {
		EliteScoreEntry ese = new EliteScoreEntry(_entries.size() + 1, amt, msg);
		ese.setBonus(isBonus);
		_entries.add(ese);
	}
}