// Copyright 2009, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS takeoff/landing data.
 * @author Luke
 * @version 11.0
 * @since 2.8
 */

public class TakeoffLanding extends DatabaseBean {
	
	private boolean _isTakeoff;
	private Instant _eventTime;

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
	public Instant getDate() {
		return _eventTime;
	}

	/**
	 * Updates the takeoff/landing date.
	 * @param dt the takeoff/landing date/time
	 */
	public void setDate(Instant dt) {
		_eventTime = dt;
	}
	
	@Override
    public int hashCode() {
    	return (getHexID() + _isTakeoff).hashCode();
    }
  
    @Override
    public boolean equals(Object o) {
    	return (o instanceof TakeoffLanding tl2) && (compareTo(o) == 0) && (_isTakeoff == tl2._isTakeoff); 
    }
}