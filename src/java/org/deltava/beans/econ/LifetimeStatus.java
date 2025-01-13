// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * A bean to store lifetime Elite status for a Pilot.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class LifetimeStatus extends EliteStatus {
	
	private final EliteLifetime _lvl;

	/**
	 * Creates the bean. 
	 * @param pilotID the Pilot database ID
	 * @param el the EliteLifetime bean
	 */
	LifetimeStatus(int pilotID, EliteLifetime el) {
		super(pilotID, el.getLevel());
		_lvl = el;
	}
	
	@Override
	public boolean getIsLifetime() {
		return true;
	}

	/**
	 * Returns the name of the lifetime Elite status level. 
	 * @return the lifetime status name
	 */
	public String getLifetimeName() {
		return _lvl.getName();
	}
}