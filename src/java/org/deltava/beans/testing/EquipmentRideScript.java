// Copyright 2005, 2009, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * A bean to store Check Ride scripts.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class EquipmentRideScript extends CheckRideScript {

	private String _eqType;

	/**
	 * Creates a new Check Ride script.
	 * @param eqType the equipment type
	 * @throws NullPointerException if eqType is null
	 * @see EquipmentRideScript#setEquipmentType(String)
	 */
	public EquipmentRideScript(String eqType) {
		super(null);
		setEquipmentType(eqType);
	}

	/**
	 * Returns the equipment type for this script.
	 * @return the equipment type
	 * @see EquipmentRideScript#setEquipmentType(String)
	 */
	public String getEquipmentType() {
		return _eqType;
	}

	/**
	 * Returns the Equipment Program for this check ride script.
	 * @return the equipment program name
	 * @see EquipmentRideScript#setProgram(String)
	 */
	public String getProgram() {
		return _programName;
	}

	/**
	 * Sets the equipment program for this check ride script.
	 * @param eqType the equipment program name
	 * @see EquipmentRideScript#getProgram()
	 */
	public void setProgram(String eqType) {
		_programName = eqType;
	}

	/**
	 * Sets the aircraft type for this check ride script.
	 * @param eqType the aircraft type
	 * @throws NullPointerException if eqType is null
	 * @see EquipmentRideScript#getEquipmentType()
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType.trim();
	}

	/**
	 * Compares two check ride scripts by comparing their equipment types.
	 * @param cs2 the script
	 * @return TRUE if the equipment type and programs match, otherwise FALSE
	 */
	public int compareTo(EquipmentRideScript cs2) {
		int tmpResult = super.compareTo(cs2);
		return (tmpResult == 0) ? _eqType.compareTo(cs2._eqType) : tmpResult;
	}

	@Override
	public String getAuditID() {
		return _programName + "!!" + _eqType;
	}
}