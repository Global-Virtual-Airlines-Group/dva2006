// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store units of weight.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public enum WeightUnit implements Unit {
	
	LB("Pound", 1), KG("Kilogram", 0.453592);

	private final String _name;
	private final double _factor;

	WeightUnit(String name, double factor) {
		_name = name;
		_factor = factor;
	}
	
	/**
	 * Returns the unit name.
	 * @return the full name
	 */
	public String getUnitName() {
		return _name;
	}
	
	/**
	 * Returns the ratio of this distance unit to pounds.
	 * @return the ratio
	 */
	public double getFactor() {
		return _factor;
	}
	
	public String getComboName() {
		return _name;
	}
	
	public String getComboAlias() {
		return name();
	}
}