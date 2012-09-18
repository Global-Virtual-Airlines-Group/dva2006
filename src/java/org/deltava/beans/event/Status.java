// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

/**
 * An enumeration for Online Event statuses.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum Status {
	OPEN, CANCELED, CLOSED, ACTIVE, COMPLETE;
	
	private final String _name;
	
	Status() {
		_name = name().substring(0, 1) + name().toLowerCase().substring(1); 
	}

	/**
	 * Returns the friendly status.
	 * @return a properly cased status
	 */
	public String getName() {
		return _name;
	}
}