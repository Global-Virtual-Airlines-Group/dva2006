// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to store Airspace types.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public enum AirspaceType implements ComboAlias {
	P("Prohibited"), R("Restricted"), Q("Danger"), CTR("Center"), D("Class D"), C("Class C"), B("Class B"), A("Class A"), E("Class E"), G("Class G");
	
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
	
	/**
	 * Returns the airspace type depending on altitude.
	 * @param agl the altitude above ground level
	 * @param msl the altitude above mean sea level
	 * @return the AirspaceType
	 */
	public static AirspaceType fromAltitude(int agl, int msl) {
		if (agl < 1200)
			return G;
		else if ((msl < 18000) || (msl > 60000))
			return E;
		else
			return A;
	}

	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return _name;
	}
}