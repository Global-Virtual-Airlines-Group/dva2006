// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration to track Route pair types.
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

public enum RoutePairType {
	UNKNOWN(false, false), PRESENT(true, false), HISTORIC(false, true), HYBRID(true, true);
	
	private final boolean _hasCurrent;
	private final boolean _hasHistoric;
	
	/**
	 * Creates the enumeration entry.
	 * @param hasCurrent TRUE if has current flights, otherwise FALSE
	 * @param hasHistoric TRUE if has historic flights, otherwise FALSE
	 */
	RoutePairType(boolean hasCurrent, boolean hasHistoric) {
		_hasCurrent = hasCurrent;
		_hasHistoric = hasHistoric;
	}
	
	/**
	 * Returns whether this Route pair has current flights.
	 * @return TRUE if current flights are available, otherwise FALSE
	 */
	public boolean hasCurrent() {
		return _hasCurrent;
	}
	
	/**
	 * Returns whether this Route pair has historic flights.
	 * @return TRUE if historic flights are available, otherwise FALSE
	 */
	public boolean hasHistoric() {
		return _hasHistoric;
	}
}