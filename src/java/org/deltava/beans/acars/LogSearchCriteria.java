// Copyright 2005, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS log search criteria.
 * @author Luke
 * @version 3.2
 * @since 1.0
 */

public class LogSearchCriteria implements java.io.Serializable {

	private int _pilotID;
	private Date _startDate;
	private Date _endDate;
	private boolean _isDispatch;
	
	/**
	 * Creates a new search criteria bean.
	 * @param sd the start date/time
	 * @param ed the end date/time
	 */
	public LogSearchCriteria(Date sd, Date ed) {
		super();
		_startDate = sd;
		_endDate = ed;
	}
	
	/**
	 * Creates a new search criteria bean to load all connections for a user.
	 * @param pilotID the user's database ID
	 * @param isDispatch TRUE if dispatch connections only, otherwise FALSE
	 */
	public LogSearchCriteria(int pilotID, boolean isDispatch) {
		this(null, null);
		_pilotID = pilotID;
		_isDispatch = isDispatch;
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
	public Date getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end date.
	 * @return the end date/time
	 */
	public Date getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns if only Dispatch connections should be included.
	 * @return TRUE if only Dispatch connections should be included, otherwise FALSE
	 */
	public boolean isDispatch() {
		return _isDispatch;
	}
	
	/**
	 * Sets whether only Dispatch connections should be included.
	 * @param isDispatch TRUE if only Dispatch connections should be included, otherwise FALSE
	 */
	public void setDispatch(boolean isDispatch) {
		_isDispatch = isDispatch;
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