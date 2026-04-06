// Copyright 2023, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.Gate;

/**
 * A bean to storage legacy Gate names and mappings to current Gate locations. 
 * @author Luke
 * @version 12.5
 * @since 10.5
 */

class LegacyGate extends Gate {
	
	private String _oldName;

	/**
	 * Creates the bean.
	 * @param lat the latitude
	 * @param lng the longitude
	 * @param sim the Simulator
	 * @param oldName the name of the Gate in the legacy Simulator
	 */
	LegacyGate(double lat, double lng, Simulator sim, String oldName) {
		super(lat, lng);
		setSimulator(sim);
		_oldName = oldName;
	}

	/**
	 * Returns the name of this Gate in the legacy Simulator.
	 * @return the old Gate name
	 */
	public String getOldName() {
		return _oldName;
	}
	
	/**
	 * Sets the name of the Gate in the legacy simulator.
	 * @param name the gate name
	 */
	public void setOldName(String name) {
		_oldName = name;
	}
}