// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A unique ID generator for messaging purposes.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class IDGenerator {

	private static long _lastID;

	/**
	 * Generates a unique ID. This currently returns a timestamp-derived value, but is not guaranteed to do so in the future.
	 * @return a unique number
	 */
	public synchronized static long generate() {

		// Get current time and check if it's newer than what came before
		long id = System.currentTimeMillis();
		if (id <= _lastID)
			id = _lastID + 2;

		// Return the id
		_lastID = id;
		return id;
	}
}