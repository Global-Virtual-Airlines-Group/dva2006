// Copyright 2020, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of development issue security levels.
 * @author Luke
 * @version 11.2
 * @since 9.0
 */

public enum IssueSecurity implements org.deltava.beans.EnumDescription {
	PUBLIC("Public"), USERS("Users Only"), STAFF("Staff Only");
	
	private final String _desc;
	
	/**
	 * Creates the enumeration entry.
	 * @param desc the description
	 */
	IssueSecurity(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}