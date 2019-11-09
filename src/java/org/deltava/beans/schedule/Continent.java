// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of continents.
 * @author Luke
 * @version 9.0
 * @since 8.7
 */

public enum Continent implements ComboAlias {
	AF("Africa"), AN("Antarctica"), AS("Asia"), EU("Eruope"), NA("North America"), OC("Oceania"), SA("South America");

	private String _name;
	
	Continent(String name) {
		_name = name;
	}
	
	/**
	 * Returns the continent name.
	 * @return the name
	 */
	public String getName() {
		return _name;
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