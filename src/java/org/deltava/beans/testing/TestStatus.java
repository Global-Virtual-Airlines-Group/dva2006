// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * An enumeration for Examination and Check Ride statuses. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum TestStatus {
	NEW, SUBMITTED, SCORED;

	private final String _name;
	
	TestStatus() {
		_name = name().substring(0, 1) + name().toLowerCase().substring(1);
	}
	
	/**
	 * Returns the status description.
	 * @return the description
	 */
	public String getName() {
		return _name;
	}
}