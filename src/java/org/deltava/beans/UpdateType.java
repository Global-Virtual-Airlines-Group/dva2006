// Copyright 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Pilot status update types.
 * @author Luke
 * @version 10.0
 * @since 8.7
 */

@Helper(StatusUpdate.class)
public enum UpdateType {
	COMMENT("Comment"), INTPROMOTION("Promotion"), RANK_CHANGE("Rank Change"), RATING_ADD("Added Rating"), RECOGNITION("Pilot Recognition"),
	STATUS_CHANGE("Status Change"), SECURITY_ADD("Added Security Role"), RATING_REMOVE("Removed Rating"), SECURITY_REMOVE("Removed Security Role"),
	EXTPROMOTION("Promotion"), AIRLINE_TX("Airline Transfer"), INACTIVITY("Inactivity Notice"), ACADEMY("Academy Update"), CERT_ADD("Pilot Certification"),
	SR_CAPTAIN("Senior Captain"), SUSPENDED("Account Suspended"), LOA("Leave of Absence"),EXT_AUTH("External Authentication"), CONTENT_WARN("Content Warning"),
	CURRENCY("Currency Policy"), ELITE("Frequent Flyer"), TOUR("Tour Completion"), ADDRINVALID("Address Invalidation");
	
	private final String _desc;
	
	/**
	 * Creates an enumeration value.
	 * @param desc the description
	 */
	UpdateType(String desc) {
		_desc = desc;
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
	public boolean getIsHidden() {
		return (this == COMMENT);
	}
}