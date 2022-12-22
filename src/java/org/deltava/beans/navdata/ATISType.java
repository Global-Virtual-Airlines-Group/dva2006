// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration of Airport ATIS types. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public enum ATISType {
	ARR("Arrival"), DEP("Departure"), COMBINED("Combined");
	
	private final String _desc;
	
	ATISType(String desc) {
		_desc = desc;
	}

	/**
	 * Returns the type description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
}