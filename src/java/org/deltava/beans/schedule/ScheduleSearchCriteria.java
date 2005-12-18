// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store search criteria for the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleSearchCriteria extends Flight {

	private int _distance;
	private int _length;
	private int _maxResults;
	
	private int _hourD;
	private int _hourA;
	
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

	public void setLength(int length) {
		if (length < 0)
			throw new IllegalArgumentException("Flight Length cannot be negative");
		
		_length = length;
	}
	
	public void setDistance(int distance) {
		if (distance > 0)
			_distance = distance;
	}
	
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