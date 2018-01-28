// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store aircraft autopilot types.
 * @author Luke
 * @version 8.2
 * @since 8.2
 */

public enum AutopilotType {
	DEFAULT("Default"), BOEING("Boeing"), MD("McDonnell-Douglas"), AIRBUS("Airbus");
	
	private final String _desc;
	
	AutopilotType(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the autopilot description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Exception-safe value parser.
	 * @param ap a type name
	 * @return the AutopilotType, or DEFAULT if unknown
	 */
	public static AutopilotType from(String ap) {
		try {
			return AutopilotType.valueOf(ap.toUpperCase());
		} catch (Exception e) {
			return DEFAULT;
		}
	}
}