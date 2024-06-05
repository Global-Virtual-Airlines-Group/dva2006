// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store airframe information.
 * @author Luke
 * @version 11.2
 * @since 10.4
 */

public class Airframe implements java.io.Serializable, UseCount, ComboAlias, Comparable<Airframe> {
	
	private final String _eqType;
	private final String _tailCode;
	private final String _id;
	private String _sdk = "Generic";
	
	private int _useCount;
	private Instant _lastUse;
	
	/**
	 * Creates the bean.
	 * @param eqType the aricraft type
	 * @param tailCode the aircraft tail code
	 * @param sbID the SimBrief airframe ID 
	 */
	public Airframe(String eqType, String tailCode, String sbID) {
		super();
		_id = sbID;
		_eqType = eqType;
		_tailCode = tailCode.toUpperCase();
	}

	/**
	 * Returns the equipment type for this airframe.
	 * @return the aircraft name
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns the SimBrief airframe ID.
	 * @return the airframe ID
	 */
	public String getSimBriefID() {
		return _id;
	}
	
	/**
	 * Returns the aircraft tail code.
	 * @return the tail code
	 */
	public String getTailCode() {
		return _tailCode;
	}
	
	/**
	 * Returns the ACARS SDK name.
	 * @return the SDK name
	 */
	public String getSDK() {
		return _sdk;
	}
	
	@Override
	public int getUseCount() {
		return _useCount;
	}

	/**
	 * Returns the date this airframe was last used in a SimBrief dispatch package.
	 * @return the last usage date/time
	 */
	public Instant getLastUse() {
		return _lastUse;
	}
	
	/**
	 * Updates the ACARS SDK used with this airframe.
	 * @param sdkName the SDK name
	 */
	public void setSDK(String sdkName) {
		_sdk = sdkName;
	}

	/**
	 * Updates the number of times this airframe has been used in a SimBrief dispatch package.
	 * @param cnt the number of flights
	 */
	public void setUseCount(int cnt) {
		_useCount = cnt;
	}
	
	/**
	 * Updates the date this airframe was last used in a SimBrief dispatch package.
	 * @param dt the last usage date/time
	 */
	public void setLastUse(Instant dt) {
		_lastUse = dt;
	}
	
	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder(_tailCode);
		if (_useCount > 0) {
			buf.append(" (");
			buf.append(_useCount);
			buf.append(" flights)");
		}
		
		return buf.toString();
	}
	
	@Override
	public String getComboAlias() {
		return _tailCode;
	}
	
	@Override
	public int hashCode() {
		return _tailCode.hashCode();
	}
	
	@Override
	public String toString() {
		return _tailCode;
	}
	
	@Override
	public int compareTo(Airframe a) {
		int tmpResult = Integer.compare(_useCount, a._useCount);
		return (tmpResult == 0) ? _tailCode.compareTo(a._tailCode) : tmpResult;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Airframe a) && _tailCode.equals(a._tailCode);
	}
}