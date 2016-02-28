// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.util.StringUtils;

/**
 * An enumeration of Runway surface types.
 * @author Luke
 * @version 6.4
 * @since 6.4
 */

public enum Surface {
	ASPHALT(true), BITUMINOUS(true), BRICK, CLAY, CEMENT(true), CONCRETE(true), CORAL, DIRT,	 GRASS, GRAVEL, ICE, MACADAM, OIL_TREATED, 
	PLANKS, SAND, SHALE, SNOW, STEEL_MATS, TARMAC(true), UNKNOWN, WATER;
	
	private final boolean _hard;
	
	/**
	 * Creates a new soft Surface.
	 */
	Surface() {
		this(false);
	}
	
	/**
	 * Creates a hard surface.
	 * @param isHard TRUE if hard, otherwise FALSE
	 */
	Surface(boolean isHard) {
		_hard = isHard;
	}
	
	/**
	 * Returns the surface name.
	 * @return the name
	 */
	public String getName() {
		return StringUtils.properCase(name().replace('_', ' '));
	}
	
	/**
	 * Returns whether this is a hard/paved surface.
	 * @return TRUE if paved, otherwise FALSE
	 */
	public boolean isHard() {
		return _hard;
	}
}