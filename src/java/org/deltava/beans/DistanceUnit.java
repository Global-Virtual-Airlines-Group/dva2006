// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store units of distance. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum DistanceUnit implements ComboAlias{
	
	MI("Statute Mile", 1), NM("Nautical Mile", 0.868976242), KM("Kilometer", 1.609344);
	
	private final String _name;
	private final double _factor;
	
	DistanceUnit(String name, double factor) {
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
	 * Returns the ratio of this distance unit to statute miles.
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