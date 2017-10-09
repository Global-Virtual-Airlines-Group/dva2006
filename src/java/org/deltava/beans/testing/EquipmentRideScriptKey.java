// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.Auditable;

import org.deltava.util.StringUtils;

/**
 * A class to generate and parse compound keys for equipment Check Ride scripts. 
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class EquipmentRideScriptKey implements Auditable {

	private final String _programName;
	private final String _eqType;
	private final boolean _isCurrency;
	
	/**
	 * Parses an encoded equipment Check Ride script key.
	 * @param key the key
	 * @return an EquipmentRideScriptKey bean
	 */
	public static EquipmentRideScriptKey parse(String key) {
		if (StringUtils.isEmpty(key)) return null;
		
		int pos = key.indexOf("!!");
		boolean isC = key.endsWith("-C");
		String eq = key.substring(pos + 2, isC ? key.length() - 2 : key.length());
		return new EquipmentRideScriptKey(key.substring(0,  pos), eq, isC);
	}
	
	/**
	 * Checks whether a Check Ride script key is valid.
	 * @param id the ID
	 * @return TRUE if valid, otherwise FALSE
	 */
	public static boolean isValid(String id) {
		return (id != null) && (id.indexOf("!!") > 1);
	}
	
	/**
	 * Creates the ride script key.
	 * @param programName the equipment program name
	 * @param eqType the equipment type
	 * @param isCurrency TRUE if a currency check ride, otherwise FALSE
	 */
	public EquipmentRideScriptKey(String programName, String eqType, boolean isCurrency) {
		super();
		_programName = programName;
		_eqType = eqType;
		_isCurrency = isCurrency;
	}
	
	/**
	 * Returns the equipment program name.
	 * @return the program name
	 */
	public String getProgramName() {
		return _programName;
	}
	
	/**
	 * Returns the aircraft type.
	 * @return the type
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns whether this is a currency Check Ride script.
	 * @return TRUE if currency, otherwise FALSE
	 */
	public boolean isCurrency() {
		return _isCurrency;
	}

	@Override
	public String getAuditID() {
		StringBuilder buf = new StringBuilder(_programName);
		buf.append("!!");
		buf.append(_eqType);
		if (_isCurrency)
			buf.append("-C");
		
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return getAuditID().hashCode();
	}
	
	@Override
	public String toString() {
		return getAuditID();
	}
}