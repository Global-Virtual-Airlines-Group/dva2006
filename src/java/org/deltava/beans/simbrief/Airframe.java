// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import java.time.Instant;

/**
 * A bean to store SimBrief airframe information.
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public class Airframe implements java.io.Serializable, Comparable<Airframe> {
	
	private final String _tailCode;
	private final String _id;
	
	private int _useCount;
	private Instant _lastUse;
	
	/**
	 * Creates the bean.
	 * @param tailCode the aircraft tail code
	 * @param id the SimBrief airframe ID 
	 */
	public Airframe(String tailCode, String id) {
		super();
		_id = id;
		_tailCode = tailCode;
	}

	/**
	 * Returns the SimBrief airframe ID.
	 * @return the airframe ID
	 */
	public String getID() {
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
	 * Returns the number of times this airframe has been used in a SimBrief dispatch package.
	 * @return the number of flights
	 */
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
	public int compareTo(Airframe a2) {
		int tmpResult = Integer.compare(_useCount, a2._useCount);
		return (tmpResult == 0) ? _tailCode.compareTo(a2._tailCode) : tmpResult;
	}
}