// Copyright 2012, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store map types.
 * @author Luke
 * @version 11.6
 * @since 5.0
 */

public enum MapType implements EnumDescription {
	NONE("None"), GOOGLE("Google Maps"), FALLINGRAIN("Falling Rain"), GOOGLEStatic("Google Static");

	private final String _desc;
	
	MapType(String name) {
		_desc = name;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}