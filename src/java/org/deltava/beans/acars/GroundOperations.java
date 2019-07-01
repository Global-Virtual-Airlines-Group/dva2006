// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store ACARS Ground Operations flags. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public enum GroundOperations {
	CATERING(0, "Catering"), FUEL(1, "Fueling"), DEICE(2, "De-Icing"), BOARD(3, "Boarding"), DEBOARD(4, "Deboarding");
	
	private final int _mask;
	private final String _desc;
	
	GroundOperations(int bit, String desc) {
		_mask = 1 << bit;
		_desc = desc;
	}
	
	/**
	 * Returns the bitmask for this flag.
	 * @return the bitmask
	 */
	public int getMask() {
		return _mask;
	}
	
	/**
	 * Returns the description for this Ground Operation.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Adds a flag to a compound bitmap.
	 * @param flags the bitmap
	 * @return the bitmap with the flag
	 */
	public int add(int flags) {
		return (flags | _mask);
	}
	
	/**
	 * Returns whether a compound bitmap has this flag.
	 * @param flags the bitmap
	 * @return TRUE whether the bitmap has the flag, otherwise FALSE
	 */
	public boolean has(int flags) {
		return ((flags & _mask) != 0);
	}
}