// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store Flight Report Elite point data.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class FlightEliteScore extends DatabaseBean implements AuthoredBean, EliteTotals {
	
	private int _pilotID;
	private String _level;
	private int _year;
	private int _distance;
	
	private final Collection<EliteScoreEntry> _entries = new TreeSet<EliteScoreEntry>();

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 */
	public FlightEliteScore(int id) {
		super();
		setID(id);
	}
	
	@Override
	public int getAuthorID() {
		return _pilotID;
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