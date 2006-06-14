// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
	
	private static final String[] SORT_NAMES = { "Random", "Flight Number", "Equipment Type", "Origin", 
		"Destination", "Departure Time", "Arrival Time", "Length", "Distance" }; 
	public static final String[] SORT_CODES = { "RAND()", "FLIGHT", "EQTYPE", "AIRPORT_D", "AIRPORT_A",
		"TIME_D", "TIME_A", "FLIGHT_TIME", "DISTANCE"};
	public static final List SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODES);
	
	public static final List HOURS = ComboUtils.fromArray(new String[] {"-", "Midnight", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM",
			"7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "Noon", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM", "9 PM",
			"10 PM", "11 PM"}, new String[] {"-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23"});

	private int _distance;
	private int _length;
	private int _maxResults;
	
	private int _hourD = -1;
	private int _hourA = -1;
	
	private String _sortBy;

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
	
	public String getSortBy() {
		return _sortBy;
	}
	
	public final int getDistance() {
		return _distance;
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

	/**
	 * Sets the
	 * @param length
	 */
	public void setLength(int length) {
		if (length < 0)
			throw new IllegalArgumentException("Flight Length cannot be negative");
		
		_length = length;
	}
	
	public void setDistance(int distance) {
		if (distance > 0)
			_distance = distance;
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
     * Sets the equipment type for this flight.
     * @param eqType the aircraft type
     * @see FlightReport#getEquipmentType()
     */
    public void setEquipmentType(String eqType) {
    	if (!"-".equals(eqType))
    		super.setEquipmentType(eqType);
    }
}