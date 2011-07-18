// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;

/**
 * An ACARS Map Entry bean for ground connections.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public abstract class GroundMapEntry extends ACARSMapEntry {
	
	private int _range;

	/**
	 * Creates the bean.
	 * @param usr the connected Pilot
	 * @param loc the location
	 */
	protected GroundMapEntry(Pilot usr, GeoLocation loc) {
		super(loc);
		_usr = usr;
	}

	/**
	 * Returns the service range.
	 * @return the range in miles
	 */
	public int getRange() {
		return _range;
	}

	/**
	 * Updates the service range.
	 * @param range the range radius in miles
	 */
	public void setRange(int range) {
		_range = range;
	}
}