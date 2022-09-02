// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import org.deltava.beans.ComboAlias;

/**
 * A bean to store flight plan types and names. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class FlightPlan implements ComboAlias, Comparable<FlightPlan> {

	private final String _fileName;
	private final String _type;
	
	/**
	 * Creates the bean.
	 * @param type the flight plan type
	 * @param name the filename
	 */
	FlightPlan(String type, String name) {
		super();
		_fileName = name;
		_type = type;
	}
	
	/**
	 * Return the plan filename.
	 * @return the filename
	 */
	public String getFileName() {
		return _fileName;
	}
	
	/**
	 * Returns the plan format type.
	 * @return the format
	 */
	public String getType() {
		return _type;
	}
	
	@Override
	public String getComboName() {
		return _type;
	}
	
	@Override
	public String getComboAlias() {
		return _fileName;
	}
	
	@Override
	public int hashCode() {
		return _fileName.hashCode();
	}
	
	@Override
	public int compareTo(FlightPlan fp2) {
		int tmpResult = _type.compareTo(fp2._type);
		return (tmpResult == 0) ? _fileName.compareTo(fp2._fileName) : tmpResult;
	}
}