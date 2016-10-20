// Copyright 2007, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to store MS Flight Simulator Fuel Tank codes.
 * @author Luke
 * @version 7.2
 * @since 2.0
 */

public enum FuelTank implements ComboAlias {

	CENTER(0, "Center"), CENTER_2(7, "Center 2"), CENTER_3(8, "Center 3"),
	LEFT_MAIN(1, "Left Main"), LEFT_AUX(2, "Left Aux"), LEFT_TIP(3, "Left Tip"),
	RIGHT_MAIN(4, "Right Main"), RIGHT_AUX(5, "Right Aux"), RIGHT_TIP(6, "Right Tip"),
	EXTERNAL(9, "External"), EXTERNAL_2(10, "External 2");
    
    private final int _code;
    private final String _name;
    
	/**
	 * Creates a Fuel Tank.
	 * @param code the event code
	 * @param name the tank name
	 */
	FuelTank(int code, String name) {
		_code = code;
		_name = name;
	}

	/**
	 * Returns the tank code.
	 * @return the code
	 */
	public int code() {
		return _code;
	}
	
	/**
	 * Returns the tank name.
	 * @return the tank name
	 */
	public String getName() {
		return _name;
	}
	
	@Override
	public String getComboName() {
		return getName();
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
	
	/**
	 * Retreives a Fuel Tank by name.
	 * @param s the name
	 * @return a FuelTank
	 */
	public static FuelTank get(String s) {
		return Enum.valueOf(FuelTank.class, s.replace(' ', '_').toUpperCase());
	}
}