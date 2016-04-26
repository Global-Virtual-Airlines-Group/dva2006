// Copyright 2005, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS log search criteria.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class LogSearchCriteria implements java.io.Serializable {

	private int _pilotID;
	private final Instant _startDate;
	private final Instant _endDate;
	
	/**
	 * Creates a new search criteria bean.
	 * @param sd the start date/time
	 * @param ed the end date/time
	 */
	public LogSearchCriteria(Instant sd, Instant ed) {
		super();
		_startDate = sd;
		_endDate = ed;
	}
	
	/**
	 * Creates a new search criteria bean to load all connections for a user.
	 * @param pilotID the user's database ID
	 */
	public LogSearchCriteria(int pilotID) {
		this(null, null);
		_pilotID = pilotID;
	}

	/**
	 * Returns the Pilot's database ID.
	 * @return the database ID
	 * @see LogSearchCriteria#setPilotID(int)
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns the start date.
	 * @return the start date/time
	 */
	public Instant getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end date.
	 * @return the end date/time
	 */
	public Instant getEndDate() {
		return _endDate;
	}
	
	/**
	 * Updates the Pilot ID to search for.
	 * @param id the Pilot's database ID, or zero
	 * @throws IllegalArgumentException if id is negative
	 * @see LogSearchCriteria#getPilotID()
	 */
	public void setPilotID(int id) {
		if (id != 0)
			DatabaseBean.validateID(_pilotID, id);
		
		_pilotID = id;
	}
}