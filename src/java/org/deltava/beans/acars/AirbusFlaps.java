// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration of Airbus flap/slat positions.
 * @author Luke
 * @version 10.6
 * @since 10.6
 */

public enum AirbusFlaps implements org.deltava.beans.EnumDescription {
	UP(0, 0, "UP"), ONE(18, 0, "1"), ONEF(18, 10, "1F"), TWO(22, 15, "2"), THREE(22, 20, "3"), FULL(27, 35, "FULL");
	
	private final int _slats;
	private final int _flaps;
	private final String _desc;
	
	AirbusFlaps(int s, int f, String desc) {
		_slats = s;
		_flaps = f;
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the flap deflection for this setting.
	 * @return the deflection in degrees
	 */
	public int getFlaps() {
		return _flaps;
	}
	
	/**
	 * Returns the slat extension for this setting.
	 * @return the extension in degrees
	 */
	public int getSlats() {
		return _slats;
	}
	
	/**
	 * Converts a magic flap code to an Airbus flap setting. 
	 * @param c the code
	 * @return an AirbusFlaps value
	 */
	public static AirbusFlaps fromCode(int c) {
		if (c >= 0)
			return UP;
		
		return switch(c) {
			case -1 -> ONE;
			case -2 -> ONEF;
			case -4 -> TWO;
			case -7 -> THREE;
			default -> FULL;
		};
	}
}