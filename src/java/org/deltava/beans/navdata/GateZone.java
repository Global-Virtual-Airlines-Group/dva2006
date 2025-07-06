// Copyright 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration of Gate types, to handle special customs/immigration zones. 
 * @author Luke
 * @version 12.1
 * @since 10.0
 */

public enum GateZone implements org.deltava.beans.EnumDescription {
	DOMESTIC("Domestic"), INTERNATIONAL("International"), USPFI("US Departures"), SCHENGEN("Schengen"), HYBRID("Domestic/International");
	
	private final String _desc;
	
	/**
	 * Creates the enumeration value.
	 * @param desc the description
	 */
	GateZone(String desc) {
		_desc = desc;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Checks whether this Zone matches another Zone. This is used to handle hybrid Domestic/International gates. 
	 * @param gz the GateZone
	 * @return TRUE if the Zones are equal or a hybrid match, otherwise FALSE
	 */
	public boolean matches(GateZone gz) {
		if (this == gz) return true;
		return (this == HYBRID) && ((gz == INTERNATIONAL) || (gz == DOMESTIC));
	}
}