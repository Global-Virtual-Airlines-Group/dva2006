// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.ComboUtils;

/**
 * A bean to store search criteria for the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleSearchCriteria extends Flight {

	private static final String[] SORT_NAMES = {"Random", "Flight Number", "Equipment Type", "Origin", "Destination",
			"Departure Time", "Arrival Time", "Length", "Distance"};
	public static final String[] SORT_CODES = {"RAND()", "FLIGHT", "EQTYPE", "AIRPORT_D", "AIRPORT_A", "TIME_D",
			"TIME_A", "FLIGHT_TIME", "DISTANCE"};
	public static final List SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODES);

	public static final List HOURS = ComboUtils.fromArray(new String[] { "-", "Midnight", "1 AM", "2 AM", "3 AM",
			"4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "Noon", "1 PM", "2 PM", "3 PM", "4 PM",
			"5 PM", "6 PM", "7 PM", "8 PM", "9 PM", "10 PM", "11 PM" },
			new String[] { "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
					"16", "17", "18", "19", "20", "21", "22", "23" });

	private int _distance;
	private int _distanceRange = 150;
	private int _length;
	private int _maxResults;
	private boolean _includeAcademy;
	private boolean _includeHistoric;

	private int _hourD = -1;
	private int _hourA = -1;

	private String _sortBy;
	private String _dbName;
	private final Collection<String> _eqTypes = new LinkedHashSet<String>();

	/**
	 * Initializes the search criteria.
	 * @param aCode
	 * @param fNumber
	 * @param leg
	 */
	public ScheduleSearchCriteria(Airline aCode, int fNumber, int leg) {
		super(aCode, fNumber, leg);
		setLeg(leg);
	}
	
	public ScheduleSearchCriteria(String sortBy) {
		super(null, 0, 0);
		setSortBy(sortBy);
	}
	
	public String getDBName() {
		return _dbName;
	}

	public String getSortBy() {
		return _sortBy;
	}

	public final int getDistance() {
		return _distance;
	}
	
	public final int getDistanceRange() {
		return _distanceRange;
	}
	
	public boolean getIncludeAcademy() {
		return _includeAcademy;
	}
	
	public boolean getIncludeHistoric() {
		return _includeHistoric;
	}

	public Date getTimeD() {
		Calendar cld = Calendar.getInstance();
		cld.set(Calendar.HOUR_OF_DAY, _hourD);
		cld.set(Calendar.MINUTE, 0);
		cld.set(Calendar.SECOND, 0);
		return cld.getTime();
	}

	public int getHourD() {
		return _hourD;
	}

	public Date getTimeA() {
		Calendar cld = Calendar.getInstance();
		cld.set(Calendar.HOUR_OF_DAY, _hourA);
		cld.set(Calendar.MINUTE, 0);
		cld.set(Calendar.SECOND, 0);
		return cld.getTime();
	}

	public int getHourA() {
		return _hourA;
	}

	public final int getLength() {
		return _length;
	}

	public final int getMaxResults() {
		return _maxResults;
	}

	public void setSortBy(String sortBy) {
		_sortBy = sortBy;
	}

	public void setHourD(int hour) {
		_hourD = hour;
	}

	public void setHourA(int hour) {
		_hourA = hour;
	}

	public Collection<String> getEquipmentTypes() {
		return _eqTypes;
	}

	public void setDBName(String db) {
		_dbName = db;
	}
	
	/**
	 * Sets the length of the flight.
	 * @param length the length in hours multiplied by ten
	 */
	public void setLength(int length) {
		if (length < 0)
			throw new IllegalArgumentException("Flight Length cannot be negative");

		_length = length;
	}

	/**
	 * Sets the distance of the flight.
	 * @param distance the distance in miles
	 */
	public void setDistance(int distance) {
		_distance = Math.max(0, distance);
	}
	
	public void setDistanceRange(int range) {
		_distanceRange = Math.max(0, range);
	}

	/**
	 * Sets the maximum number of schedule entries to return.
	 * @param results the number of entries
	 * @throws IllegalArgumentException if results is negative
	 */
	public void setMaxResults(int results) {
		if (results < 0)
			throw new IllegalArgumentException("Result Size cannot be negative");

		_maxResults = results;
	}

	/**
	 * Adds an equipment type to search with.
	 * @param eqType the aircraft type
	 * @see ScheduleSearchCriteria#getEquipmentTypes()
	 */
	public void addEquipmentType(String eqType) {
		if (!"-".equals(eqType))
			_eqTypes.add(eqType);
	}

	/**
	 * Updates the equipment types to search with.
	 * @param eqTypes a Collection of equipment type codes
	 * @see ScheduleSearchCriteria#getEquipmentTypes()
	 */
	public void setEquipmentTypes(Collection<String> eqTypes) {
		if (eqTypes != null) {
			_eqTypes.clear();
			_eqTypes.addAll(eqTypes);
		}
	}

	/**
	 * Returns the equipment type.
	 * @return the first equipment type
	 */
	public final String getEquipmentType() {
		return (_eqTypes.isEmpty()) ? null : _eqTypes.iterator().next();
	}

	/**
	 * Adds an equipment type to the criteria.
	 * @param eqType the equipment type
	 * @see ScheduleSearchCriteria#addEquipmentType(String)
	 */
	public final void setEquipmentType(String eqType) {
		addEquipmentType(eqType);
	}
	
	/**
	 * Includes Flight Academy flights in the search.
	 * @param doInclude TRUE if Academy flights should be included, otherwise FALSE
	 */
	public void setIncludeAcademy(boolean doInclude) {
		_includeAcademy = doInclude;
	}
	
	/**
	 * Includes Historic flights in the search.
	 * @param doInclude TRUE if Historic flights should be included, otherwise FALSE
	 */
	public void setIncludeHistoric(boolean doInclude) {
		_includeHistoric = doInclude;
	}
}