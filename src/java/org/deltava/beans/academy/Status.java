// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

/**
 * An enumeration for Flight Academy course statuses. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum Status {
	STARTED("In Progress"), ABANDONED("Abandoned"), COMPLETE("Complete"), PENDING("Pending");

	private final String _name;
	
	Status(String name) {
		_name = name;
	}
	
	/**
	 * Returns the friendly status name.
	 * @return the status name
	 */
	public String getName() {
		return _name;
	}
}