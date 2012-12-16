// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration for Navigation Data element types.
 * @author Luke
 * @version 5.1
 * @since 5.0
 */

public enum Navaid {
	AIRPORT("Airport"), VOR("VOR"), NDB("NDB"), INT("Intersection"), RUNWAY("Runway"), GATE("Gate");

	private final String _name;
	
	Navaid(String name) {
		_name = name;
	}
	
	/**
	 * Returns the type name.
	 * @return the type name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Retrieves a Navaid type based on its name.
	 * @param name the name
	 * @return a Navaid or INT if not found
	 */
	public static Navaid fromName(String name) {
		if (name == null) return INT;
		for (Navaid n : values()) {
			if (name.equals(n._name) || (name.equals(n.name())))
				return n;
		}
		
		return INT;
	}
}