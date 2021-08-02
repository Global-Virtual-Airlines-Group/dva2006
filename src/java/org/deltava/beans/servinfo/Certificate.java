// Copyright 2009, 2010, 2011, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store VATSIM registration data.
 * @author Luke
 * @version 10.1
 * @since 2.6
 */

public class Certificate extends DatabaseBean {
	
	private boolean _active;
	private Instant _regDate;
	
	private final Collection<String> _pilotRatings = new TreeSet<String>();
	
	/**
	 * Initializes the bean.
	 * @param id the user's VATSIM ID
	 */
	public Certificate(int id) {
		setID(id);
	}
	
	/**
	 * Returns the user's registration date.
	 * @return the registration date/time
	 */
	public Instant getRegistrationDate() {
		return _regDate;
	}
	
	/**
	 * Returns the user's pilot ratings.
	 * @return a Collection of rating codes
	 */
	public Collection<String> getPilotRatings() {
		return new ArrayList<String>(_pilotRatings);
	}
	
	/**
	 * Returns whether the user account is active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean isActive() {
		return _active;
	}
	
	/**
	 * Adds a Pilot rating.
	 * @param rating the rating code
	 */
	public void addPilotRating(String rating) {
		_pilotRatings.add(rating.startsWith("P") ? rating : ("P" + rating));
	}
	
	/**
	 * Updates the user's registration date.
	 * @param dt the registration date/time
	 */
	public void setRegistrationDate(Instant dt) {
		_regDate = dt;
	}
	
	/**
	 * Updates whether the user account is active.
	 * @param active TRUE if active, otherwise FALSE
	 */
	public void setActive(boolean active) {
		_active = active;
	}
}