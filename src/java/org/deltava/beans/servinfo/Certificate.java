// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.Date;

/**
 * A bean to store VATSIM registration data.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class Certificate extends NetworkUser {
	
	private boolean _active;
	private String _eMail;
	private Date _regDate;
	
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
	 * Returns whether the user account is active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean isActive() {
		return _active;
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

	public int getType() {
		return NetworkUser.PILOT;
	}

	public String getIconColor() {
		return BLUE;
	}

	public String getInfoBox() {
		return null;
	}
}