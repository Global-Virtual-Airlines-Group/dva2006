// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration to store Airspace types.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public enum AirspaceType {
	P("Prohibited"), R("Restricted"), Q("Danger"), CTR("Center"), A("Class A"), B("Class B"), C("Class C"), D("Class D");
	
	private final String _name;
	
	AirspaceType(String name) {
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
	 * Retrieves n AirspaceType type based on its name.
	 * @param name the name
	 * @return an Airspacetype, or null if unknown
	 */
	public static AirspaceType fromName(String name) {
		if (name == null) return null;
		for (AirspaceType t : values()) {
			if (name.equals(t._name) || (name.equals(t.name())))
				return t;
		}
		
		return null;
	}
}