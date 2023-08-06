// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * A bean to store data about an Elite program. This is used to share data with the ACARS server which does not have access to each
 * virtual airlne's SystemData object. 
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class EliteProgram implements java.io.Serializable {
	
	private final String _name;
	private String _distanceUnit;
	private String _pointUnit;

	/**
	 * Creates the bean.
	 * @param name the program name 
	 */
	public EliteProgram(String name) {
		super();
		_name = name;
	}

	/**
	 * Returns the program name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the distance unit name.
	 * @return the unit name
	 */
	public String getDistanceUnit() {
		return _distanceUnit;
	}
	
	/**
	 * Returns the point unit name.
	 * @return the unit name
	 */
	public String getPointUnit() {
		return _pointUnit;
	}

	/**
	 * Updates the unit names.
	 * @param dst the distance unit name
	 * @param pts the point unit name
	 */
	public void setUnits(String dst, String pts) {
		_distanceUnit = dst;
		_pointUnit = pts;
	}
}