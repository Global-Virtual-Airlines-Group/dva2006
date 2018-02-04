// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store flight load factor types.
 * @author Luke
 * @version 8.2
 * @since 8.2
 */

public enum LoadType {
	NONE, ASSIGNED, ACTUAL, MANUAL, RANDOM;

	/**
	 * Proper-cased name.
	 * @return the name
	 */
	public String getName() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
}