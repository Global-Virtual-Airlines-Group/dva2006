// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * An enumeration to store Check Ride types. 
 * @author Luke
 * @version 5.3
 * @since 5.3
 */

public enum RideType {
	CHECKRIDE("Check Ride"), HIRE("Initial Hire"), WAIVER("Check Ride Waiver"), CURRENCY("Currency Check");
	
	private final String _name;
	
	RideType(String name) {
		_name = name;
	}
	
	/**
	 * Returns the Check Ride type name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
}