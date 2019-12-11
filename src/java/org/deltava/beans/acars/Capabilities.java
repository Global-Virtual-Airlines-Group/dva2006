// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store Simulator capabilities.
 * @author Luke
 * @version 9.0
 * @since 8.6
 */

public enum Capabilities {
	GSX(0, "GSX", true), RAAS(1, "RAAS", true), XSB(2, "XSquawkbox", true), FUEL_LOAD(3, "Fuel Loading"), PAX_LOAD(4, "Passenger Loading"), 
	FMC_LOAD(5, "FMC Fuel/Passenger Loaidng"), ANNOUNCE(6, "Cabin Announcements"), AFTERBURNER(7, "Afterburner"), JETWAY(8, "Jetway Control"), 
	FMC(9, "FMC", true), VPILOT(10, "vPilot", true), XIVAP(11, "X-IvAp", true), IVAP(12, "IvAp", true), CABINSIZE(13, "Cabin Size"), XPILOT(14, "xPilot", true);
	
	private final long _mask;
	private final String _desc;
	private final boolean _show;
	
	/**
	 * Creates a nonvisible capability.
	 * @param bit the bit flag
	 * @param desc the description
	 */
	Capabilities(int bit, String desc) {
		this(bit, desc, false);
	}
	
	Capabilities(int bit, String desc, boolean show) {
		_mask = 1 << bit;
		_desc = desc;
		_show = show;
	}

	/**
	 * Returns the bitmask for this flag.
	 * @return the bitmask
	 */
	public long getMask() {
		return _mask;
	}
	
	/**
	 * Returns the description for this Capability.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns if this capability is visible.
	 * @return TRUE if visible, otherwise FALSE
	 */
	public boolean isVisible() {
		return _show;
	}
	
	/**
	 * Adds a flag to a compound bitmap.
	 * @param flags the bitmap
	 * @return the bitmap with the flag
	 */
	public long add(long flags) {
		return (flags | _mask);
	}
	
	/**
	 * Returns whether a compound bitmap has this flag.
	 * @param flags the bitmap
	 * @return TRUE whether the bitmap has the flag, otherwise FALSE
	 */
	public boolean has(long flags) {
		return ((flags & _mask) != 0);
	}
}