// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store map types.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum MapType implements ComboAlias {
	GOOGLE("Google Maps"), FALLINGRAIN("Falling Rain");

	private final String _name;
	
	MapType(String name) {
		_name = name;
	}
	
	/**
	 * Returns the map type description.
	 * @return the description
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