// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Pilot status update types.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

@Helper(StatusUpdate.class)
public enum UpdateType {
	COMMENT("Comment"), INTPROMOTION("Promotion"), RANK_CHANGE("Rank Change"), RATING_ADD("Added Rating"), RECOGNITION("Pilot Recognition"),
	STATUS_CHANGE("Status Change"), SECURITY_ADD("Added Security Role"), RATING_REMOVE("Removed Rating"), SECURITY_REMOVE("Removed Security Role"),
	EXTPROMOTION("Promotion"), AIRLINE_TX("Airline Transfer"), INACTIVITY("Inactivity Notice"), ACADEMY("Academy Update"), CERT_ADD("Pilot Certification"),
	SR_CAPTAIN("Senior Captain"), SUSPENDED("Account Suspended"), LOA("Leave of Absence"),EXT_AUTH("External Authentication"), CONTENT_WARN("Content Warning"),
	CURRENCY("Currency Policy");
	
	private final String _desc;
	private final boolean _isHidden;
	
	/**
	 * Creates a public enumeration value.
	 * @param desc the description
	 */
	UpdateType(String desc) {
		this(desc, false);
	}
	
	/**
	 * Creates an enumeration value.
	 * @param desc the description
	 * @param isHidden TRUE if private, otherwise FALSE
	 */
	UpdateType(String desc, boolean isHidden) {
		_desc = desc;
		_isHidden = isHidden;
	}
	
	/**
	 * Returns the type description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}

	/**
	 * Returns whether updates of this type should be hidden to non-HR users.
	 * @return TRUE if hidden, otherwise FALSE
	 */
	public boolean isHidden() {
		return _isHidden;
	}
}