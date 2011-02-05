// Copyright 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

/**
 * A bean to store VATSIM registration data.
 * @author Luke
 * @version 3.6
 * @since 2.6
 */

public class Certificate extends NetworkUser {
	
	private boolean _active;
	private String _eMail;
	private Date _regDate;
	
	private final Collection<String> _pilotRatings = new TreeSet<String>();
	
	/**
	 * Initializes the bean.
	 * @param id the user's VATSIM ID
	 */
	public Certificate(int id) {
		super(id);
	}
	
	/**
	 * Returns the user's e-mail domain.
	 * @return the domain name
	 */
	public String getEmailDomain() {
		return _eMail;
	}
	
	/**
	 * Returns the user's registration date.
	 * @return the registration date/time
	 */
	public Date getRegistrationDate() {
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
		if (!rating.startsWith("P"))
			rating = "P" + rating;
		
		_pilotRatings.add(rating);
	}
	
	/**
	 * Updates the user's e-mail domain. Data before the @ will be stripped.
	 * @param domain the domain name
	 */
	public void setEmailDomain(String domain) {
		if ((domain != null) && (domain.indexOf('@') != -1))
			domain = domain.substring(domain.lastIndexOf('@') + 1);
		
		_eMail = domain;
	}

	/**
	 * Updates the user's registration date.
	 * @param dt the registration date/time
	 */
	public void setRegistrationDate(Date dt) {
		_regDate = dt;
	}
	
	/**
	 * Updates whether the user account is active.
	 * @param active TRUE if active, otherwise FALSE
	 */
	public void setActive(boolean active) {
		_active = active;
	}

	public Type getType() {
		return Type.PILOT;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(getID());
		buf.append(' ');
		buf.append(getName());
		buf.append(' ');
		buf.append(_eMail);
		return buf.toString();
	}
}