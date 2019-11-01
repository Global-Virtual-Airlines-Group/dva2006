// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of continents.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public enum Continent {
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
}