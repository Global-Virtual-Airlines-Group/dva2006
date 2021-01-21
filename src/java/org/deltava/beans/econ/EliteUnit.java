// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * An enumeration of Elite level prerequisite units. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public enum EliteUnit implements org.deltava.beans.EnumDescription {
	LEGS("Leg"), DISTANCE("Mile"), POINTS("Point");
	
	private final String _desc;
	
	/**
	 * Creates the entry.
	 * @param desc the description
	 */
	EliteUnit(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}