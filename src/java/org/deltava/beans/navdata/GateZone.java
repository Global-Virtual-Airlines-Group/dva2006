// Copyright 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration of Gate types, to handle special customs/immigration zones. 
 * @author Luke
 * @version 11.1
 * @since 10.0
 */

public enum GateZone implements org.deltava.beans.EnumDescription {
	DOMESTIC("Domestic"), INTERNATIONAL("International"), USPFI("US Departures"), SCHENGEN("Schengen");
	
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
}