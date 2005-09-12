// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.Flight;
import org.deltava.beans.FlightReport;

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
	
	private String _sortBy;

	/**
	 * Initializes the search criteria.
	 * @param aCode
	 * @param fNumber
	 * @param leg
	 */
	public ScheduleSearchCriteria(Airline aCode, int fNumber, int leg) {
		super(aCode, fNumber, leg);
	}
	
	public String getSortBy() {
		return _sortBy;
	}
	
	public final int getDistance() {
		return _distance;
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

	public void setLength(int length) {
		if (length < 0)
			throw new IllegalArgumentException("Flight Length cannot be negative");
		
		_length = length;
	}
	
	public void setDistance(int distance) {
		if (distance < 0)
			throw new IllegalArgumentException("Flight Distance cannot be negative");
		
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