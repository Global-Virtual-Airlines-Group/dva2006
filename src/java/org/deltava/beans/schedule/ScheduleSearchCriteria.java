// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.util.ComboUtils;

/**
 * A bean to store search criteria for the Flight Schedule.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class ScheduleSearchCriteria extends Flight {

	private static final String[] SORT_NAMES = {"Random", "Flight Number", "Equipment Type", "Origin", "Destination", "Departure Time", "Arrival Time", "Length", "Distance", "Flight Count", "Last Flown"};
	public static final String[] SORT_CODES = {"RAND()", "FLIGHT", "EQTYPE", "AIRPORT_D", "AIRPORT_A", "TIME_D", "TIME_A", "FLIGHT_TIME", "DISTANCE", "FCNT", "LF"};
	public static final List<?> SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODES);

	public static final List<?> HOURS = ComboUtils.fromArray(new String[] { "-", "Midnight", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "Noon", "1 PM", "2 PM", "3 PM", "4 PM",
			"5 PM", "6 PM", "7 PM", "8 PM", "9 PM", "10 PM", "11 PM" }, new String[] { "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" });

	private int _distance;
	private int _distanceRange = 150;
	private int _length;
	private int _maxResults;
	private Inclusion _includeAcademy;
	private boolean _dispatchRouteCounts;
	private Inclusion _dispatchOnly;
	private Inclusion _excludeHistoric;
	private int _maxPerRoute;
	
	private int _pilotID;
	private boolean _notVisitedD;
	private boolean _notVisitedA;

	private int _hourD = -1;
	private int _hourA = -1;
	
	private int _routeLegs = -1;
	private int _lastFlownInterval = -1;

	private String _sortBy;
	private String _dbName;
	private final Collection<String> _eqTypes = new LinkedHashSet<String>();

	/**
	 * Initializes the search criteria.
	 * @param a the Airline
	 * @param fNumber the flight number
	 * @param leg the leg number
	 */
	public ScheduleSearchCriteria(Airline a, int fNumber, int leg) {
		super(a, fNumber, leg);
		setLeg(0);
	}
	
	public ScheduleSearchCriteria(String sortBy) {
		super(null, 0, 0);
		_sortBy = sortBy;
		setLeg(0);
	}
	
	/**
	 * Returns the database to search.
	 * @return the database name
	 */
	public String getDBName() {
		return _dbName;
	}

	/**
	 * Returns the SQL sorting clause.
	 * @return the sorting SQL
	 */
	public String getSortBy() {
		return _sortBy;
	}

	@Override
	public final int getDistance() {
		return _distance;
	}
	
	public final int getDistanceRange() {
		return _distanceRange;
	}
	
	/**
	 * Returns the Pilot ID, if filtering for unvisited airports.
	 * @return the Pilot's database ID
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns whether to only search routes including an airport not previously visited.
	 * @return TRUE if only including unvisited departure Airports, otherwise FALSE
	 */
	public boolean getNotVisitedD() {
		return _notVisitedD;
	}
	
	/**
	 * Returns whether to only search routes including an airport not previously visited.
	 * @return TRUE if only including unvisited arrival Airports, otherwise FALSE
	 */
	public boolean getNotVisitedA() {
		return _notVisitedA;
	}
	
	public boolean getCheckDispatch() {
		return _dispatchRouteCounts;
	}
	
	/**
	 * Returns whether to exclude historic flights from the search.
	 * @return an Inclusion
	 */
	public Inclusion getExcludeHistoric() {
		return _excludeHistoric;
	}

	/**
	 * Returns whether to search for flights with dispatch flight plans.
	 * @return an Inclusion
	 */
	public Inclusion getDispatchOnly() {
		return _dispatchOnly;
	}
	
	/**
	 * Returns whether to include Flight Academy flights in the search.
	 * @return an Inclusion
	 */
	public Inclusion getIncludeAcademy() {
		return _includeAcademy;
	}
	
	/**
	 * Returns the maximum number of schedule entries per route to return.
	 * @return the maximum number of entries
	 */
	public int getFlightsPerRoute() {
		return _maxPerRoute;
	}
	
	public int getRouteLegs() {
		return _routeLegs;
	}
	
	public int getLastFlownInterval() {
		return _lastFlownInterval;
	}

	public Instant getTimeD() {
		return Instant.now().truncatedTo(ChronoUnit.HOURS).with(ChronoField.HOUR_OF_DAY, _hourD);
	}

	public int getHourD() {
		return _hourD;
	}

	public Instant getTimeA() {
		return Instant.now().truncatedTo(ChronoUnit.HOURS).with(ChronoField.HOUR_OF_DAY, _hourA);
	}

	public int getHourA() {
		return _hourA;
	}

	@Override
	public final int getLength() {
		return _length;
	}
	
	/**
     * DISABLED property.
     * @throws UnsupportedOperationException
     */
    @Override
    public final java.time.Duration getDuration() {
        throw new UnsupportedOperationException();
    }

	/**
	 * Returns the maximum numnber of schedule entries to return.
	 * @return the maximum number of results
	 */
	public int getMaxResults() {
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

	/**
	 * Returns the equipment types to search for.
	 * @return a Collection of equipment type names
	 */
	public Collection<String> getEquipmentTypes() {
		return _eqTypes;
	}

	/**
	 * Updates the database to search.
	 * @param db the database name
	 */
	public void setDBName(String db) {
		_dbName = db;
	}
	
	/**
	 * Sets the length of the flight.
	 * @param length the length in hours multiplied by ten
	 */
	public void setLength(int length) {
		_length = Math.max(0, length);
	}

	/**
	 * Sets the distance of the flight.
	 * @param distance the distance in miles
	 */
	public void setDistance(int distance) {
		_distance = Math.max(0, distance);
	}

	/**
	 * Sets the distance range of the flight.
	 * @param range the distance range in miles
	 */
	public void setDistanceRange(int range) {
		_distanceRange = Math.max(0, range);
	}
	
	/**
	 * The Pilot database ID, if filtering for unvisited airports.
	 * @param id the Pilot ID
	 */
	public void setPilotID(int id) {
		_pilotID = Math.max(0, id);
	}

	/**
	 * Sets the maximum number of schedule entries to return.
	 * @param results the number of entries
	 */
	public void setMaxResults(int results) {
		_maxResults = Math.max(1, results);
	}
	
	/**
	 * Sets the preferred number of flights per route to return.
	 * @param maxFlights the preferred number of entries
	 */
	public void setFlightsPerRoute(int maxFlights) {
		_maxPerRoute = Math.max(0, maxFlights);
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
	@Override
	public final String getEquipmentType() {
		return (_eqTypes.isEmpty()) ? null : _eqTypes.iterator().next();
	}

	/**
	 * Adds an equipment type to the criteria.
	 * @param eqType the equipment type
	 * @see ScheduleSearchCriteria#addEquipmentType(String)
	 */
	@Override
	public final void setEquipmentType(String eqType) {
		addEquipmentType(eqType);
	}
	
	/**
	 * Includes Flight Academy flights in the search.
	 * @param doInclude an Inclusion
	 */
	public void setIncludeAcademy(Inclusion doInclude) {
		_includeAcademy = doInclude;
	}
	
	/**
	 * Checks whether route pairs have Dispatch routes associated with them.
	 * @param checkDispatch TRUE if Dispatch routes should be checked, otherwise FALSE
	 */
	public void setCheckDispatchRoutes(boolean checkDispatch) {
		_dispatchRouteCounts = checkDispatch;
	}
	
	/**
	 * Excludes historic flights form the search.
	 * @param exHistoric an Inclusion
	 */
	public void setExcludeHistoric(Inclusion exHistoric) {
		_excludeHistoric = exHistoric;
	}
	
	/**
	 * Includes only flights between airports that have a Dispatch route in the database.
	 * @param dspOnly an Inclusion
	 */
	public void setDispatchOnly(Inclusion dspOnly) {
		_dispatchOnly = dspOnly;
	}
	
	/**
	 * Includes only unvisited departure airports.
	 * @param nv TRUE if only unvisited departure airports should be included, otherwise FALSE
	 * @see ScheduleSearchCriteria#setPilotID(int)
	 */
	public void setNotVisitedD(boolean nv) {
		_notVisitedD = nv;
	}

	/**
	 * Includes only unvisited arrival airports.
	 * @param nv TRUE if only unvisited arrival airports should be included, otherwise FALSE
	 * @see ScheduleSearchCriteria#setPilotID(int)
	 */
	public void setNotVisitedA(boolean nv) {
		_notVisitedA = nv;		
	}
	
	/**
	 * Sets the number of route legs flown by the pilot.
	 * @param legs the number of legs 
	 */
	public void setRouteLegs(int legs) {
		_routeLegs = Math.max(-1, legs);
	}
	
	/**
	 * Sets the interval since the last time the route was flown.
	 * @param days the number of days
	 */
	public void setLastFlownInterval(int days) {
		_lastFlownInterval = Math.max(-1, days);
	}
}