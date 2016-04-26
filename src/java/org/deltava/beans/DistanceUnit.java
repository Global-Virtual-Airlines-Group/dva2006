// Copyright 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store units of distance. 
 * @author Luke
 * @version 6.0
 * @since 5.0
 */

public enum DistanceUnit implements Unit {
	
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
	@Override
	public String getUnitName() {
		return _name;
	}
	
	/**
	 * Returns the ratio of this distance unit to statute miles.
	 * @return the ratio
	 */
	@Override
	public double getFactor() {
		return _factor;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}