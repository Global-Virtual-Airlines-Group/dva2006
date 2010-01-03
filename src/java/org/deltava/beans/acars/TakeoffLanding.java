// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS takeoff/landing data.
 * @author Luke
 * @version 2.8
 * @since 2.8
 */

public class TakeoffLanding extends DatabaseBean {
	
	private boolean _isTakeoff;
	private Date _eventTime;

	/**
	 * Initializes the bean.
	 * @param id the ACARS Flight ID
	 * @param isTakeoff TRUE if a takeoff, otherwise FALSE 
	 */
	public TakeoffLanding(int id, boolean isTakeoff) {
		super();
		setID(id);
		_isTakeoff = isTakeoff;
	}

	/**
	 * Returns whether this is a takeoff or a landing.
	 * @return TRUE if a takeoff, otherwise FALSE
	 */
	public boolean getIsTakeoff() {
		return _isTakeoff;
	}
	
	/**
	 * Returns the date of the takeoff/landing.
	 * @return the takeoff/landing date/time
	 */
	public Date getDate() {
		return _eventTime;
	}

	/**
	 * Updates the takeoff/landing date.
	 * @param dt the takeoff/landing date/time
	 */
	public void setDate(Date dt) {
		_eventTime = dt;
	}
}