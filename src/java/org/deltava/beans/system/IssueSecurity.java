// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of development issue security levels.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum IssueSecurity implements org.deltava.beans.ComboAlias {
	PUBLIC("Public"), USERS("Users Only"), STAFF("Staff Only");
	
	private final String _desc;
	
	/**
	 * Creates the enumeration entry.
	 * @param desc the description
	 */
	IssueSecurity(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns a propercased description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return getDescription();
	}
}