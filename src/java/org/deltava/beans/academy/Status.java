// Copyright 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

/**
 * An enumeration for Flight Academy course statuses. 
 * @author Luke
 * @version 10.1
 * @since 5.0
 */

public enum Status implements org.deltava.beans.EnumDescription {
	STARTED("In Progress"), ABANDONED("Abandoned"), COMPLETE("Complete"), PENDING("Pending");

	private final String _name;
	
	Status(String name) {
		_name = name;
	}

	@Override
	public String getDescription() {
		return _name;
	}
}