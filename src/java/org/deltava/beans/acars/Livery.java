// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.ViewEntry;

import org.deltava.beans.schedule.Airline;

/**
 * A bean to store ACARS multi-player airline livery data.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class Livery implements Comparable<Livery>, java.io.Serializable, ViewEntry {

	private Airline _a;
	private String _code;
	private String _desc;
	private boolean _isDefault;
	
	/**
	 * Initializes the bean.
	 * @param a the Airline
	 * @param code the livery code
	 * @throws NullPointerException if code is null
	 */
	public Livery(Airline a, String code) {
		super();
		_a = a;
		_code = code.trim().toUpperCase();
	}
	
	/**
	 * Returns the Airline.
	 * @return the Airline bean
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the livery code.
	 * @return the code
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns the livery description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns whether this is the default livery.
	 * @return TRUE if the default livery, otherwise FALSE
	 * @see Livery#setDefault(boolean)
	 */
	public boolean getDefault() {
		return _isDefault;
	}
	
	/**
	 * Updates the livery description.
	 * @param desc the description
	 * @see Livery#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates whether this is the default livery for the airline.
	 * @param isDefault TRUE if the default livery, otherwise FALSE
	 * @see Livery#getDefault()
	 */
	public void setDefault(boolean isDefault) {
		_isDefault = isDefault;
	}
	
	public int hashCode() {
		return (_a.getCode() + ":" + _code).hashCode();
	}
	
	/**
	 * Compares two Liveries by comparing their Airline, default setting and code.
	 */
	public int compareTo(Livery l2) {
		int tmpResult = _a.compareTo(l2._a);
		if (tmpResult != 0)
			tmpResult = Boolean.valueOf(_isDefault).compareTo(Boolean.valueOf(l2._isDefault)) * -1;
		
		return (tmpResult == 0) ? _code.compareTo(l2._code) : tmpResult;
	}
	
	public String getRowClassName() {
		return _isDefault ? "opt1" : null;
	}
}