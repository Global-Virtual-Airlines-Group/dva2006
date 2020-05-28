// Copyright 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store map types.
 * @author Luke
 * @version 9.0
 * @since 5.0
 */

public enum MapType implements EnumDescription {
	GOOGLE("Google Maps"), FALLINGRAIN("Falling Rain"), GOOGLEStatic("Google Static");

	private final String _desc;
	
	MapType(String name) {
		_desc = name;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}