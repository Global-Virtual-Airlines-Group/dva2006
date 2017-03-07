// Copyright 2009, 2010, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;
import java.time.Instant;
import java.text.Collator;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.Person;

/**
 * A bean to store VATSIM registration data.
 * @author Luke
 * @version 7.2
 * @since 2.6
 */

public class Certificate extends NetworkUser {
	
	private boolean _active;
	private String _eMail;
	private Instant _regDate;
	
	private final Collection<String> _pilotRatings = new TreeSet<String>();
	
	/**
	 * Initializes the bean.
	 * @param id the user's VATSIM ID
	 */
	public Certificate(int id) {
		super(id, OnlineNetwork.VATSIM);
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
	 * Updates the user's e-mail domain. Data before the @ will be stripped.
	 * @param domain the domain name
	 */
	public void setEmailDomain(String domain) {
		_eMail = ((domain != null) && (domain.indexOf('@') != -1)) ? domain.substring(domain.lastIndexOf('@') + 1) : domain;
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

	/**
	 * Performs a case- and accent-insensitive name comparison.
	 * @param p a Person
	 * @return TRUE if the names match, otherwise FALSE
	 */
	public boolean comapreName(Person p) {
		Collator cl = Collator.getInstance();
		cl.setStrength(Collator.PRIMARY);
		int tmpResult = cl.compare(getFirstName(), p.getFirstName());
		if (tmpResult == 0)
			tmpResult = cl.compare(getLastName(), p.getLastName());

		return (tmpResult == 0);
	}

	@Override
	public Type getType() {
		return Type.PILOT;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getID());
		buf.append(' ');
		buf.append(getName());
		buf.append(' ');
		buf.append(_eMail);
		return buf.toString();
	}
}