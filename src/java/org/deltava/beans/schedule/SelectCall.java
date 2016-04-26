// Copyright 2005, 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Aircraft SELCAL data and reservations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SelectCall implements java.io.Serializable, Comparable<SelectCall>, ViewEntry {

	private String _acCode;
	private String _selcalCode;
	private String _eqType;

	private int _reservedBy;
	private Instant _reserveDate;

	/**
	 * Creates a new SELCAL bean.
	 * @param aircraft the aircraft tail number
	 * @param code the SELCAL code
	 * @throws NullPointerException if aircraft or code are null
	 * @throws IllegalArgumentException if code is not 4 or 5 characters
	 */
	public SelectCall(String aircraft, String code) {
		super();
		setAircraftCode(aircraft);
		setCode(code);
	}

	/**
	 * Returns the aircraft tail number.
	 * @return the tail number
	 * @see SelectCall#setAircraftCode(String)
	 */
	public String getAircraftCode() {
		return _acCode;
	}

	/**
	 * Returns the SELCAL code for the aircraft.
	 * @return the SELCAL code
	 * @see SelectCall#setCode(String)
	 */
	public String getCode() {
		return _selcalCode;
	}
	
	/**
	 * Returns the aircraft type.
	 * @return the equipment type
	 * @see SelectCall#setEquipmentType(String)
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns the Pilot who has reserved this aircraft's SELCAL code.
	 * @return the Pilot's database ID
	 * @see SelectCall#setReservedBy(int)
	 */
	public int getReservedBy() {
		return _reservedBy;
	}
	
	/**
	 * Returns the date this aircraft's SELCAL code was reserved.
	 * @return the date/time the SELCAL code was reserved.
	 * @see SelectCall#setReservedOn(Instant)
	 */
	public Instant getReservedOn() {
		return _reserveDate;
	}
	
	/**
	 * Marks this SELCAL record as available.
	 */
	public void free() {
		_reservedBy = 0;
		_reserveDate = null;
	}
	
	/**
	 * Updates the aircraft's tail number.
	 * @param code the registration code
	 * @throws NullPointerException if code is null
	 * @see SelectCall#getAircraftCode()
	 */
	public void setAircraftCode(String code) {
		_acCode = code.trim().toUpperCase();
	}
	
	/**
	 * Updates the aircraft's SELCAL code. This method interposes
	 * @param code the SELCAL code
	 * @throws NullPointerException if code is null
	 * @throws IllegalArgumentException if code is not 4 or 5characters
	 * @see SelectCall#getCode()
	 */
	public void setCode(String code) {
		String c = code.trim().toUpperCase();
		if ((c.length() < 4) || (c.length() > 5))
			throw new IllegalArgumentException("Invalid SELCAL code - " + c);
		
		if (c.length() == 4) {
			StringBuilder buf = new StringBuilder(c.substring(0, 2));
			buf.append('-');
			buf.append(c.substring(2));
			_selcalCode = buf.toString();
		} else
			_selcalCode = code;
	}
	
	/**
	 * Updates the aircraft equipment type.
	 * @param eqType the equipment type
	 * @see SelectCall#getEquipmentType()
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}
	
	/**
	 * Updates the Pilot reserving this aircraft's SELCAL code.
	 * @param id the Pilot's database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see SelectCall#getReservedBy()
	 */
	public void setReservedBy(int id) {
		if (id != 0)
			DatabaseBean.validateID(_reservedBy, id);
		
		_reservedBy = id;
	}
	
	/**
	 * Updates the date this SELCAL code was reserved.
	 * @param dt the date/time the aircraft code was reserved
	 * @see SelectCall#getReservedOn()
	 */
	public void setReservedOn(Instant dt) {
		_reserveDate = dt;
	}
	
	/**
	 * Displays the SELCAL code.
	 */
	@Override
	public String toString() {
		return _selcalCode;
	}
	
	/**
	 * Compares two beans by comparing the SELCAL codes.
	 */
	@Override
	public int compareTo(SelectCall sc2) {
		return _selcalCode.compareTo(sc2._selcalCode);
	}
	
	/**
	 * Returns the CSS row class name if in a table view.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return (_reservedBy == 0) ? null : "opt2";
	}
}