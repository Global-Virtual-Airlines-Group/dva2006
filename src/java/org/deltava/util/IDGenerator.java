package org.deltava.util;

import org.apache.log4j.Logger;

/**
 * A unique ID generator for messaging purposes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IDGenerator {
   
   private static final Logger log = Logger.getLogger(IDGenerator.class);
	private static long _lastID;

	/**
	 * Generates a unique ID. This currently returns a timestamp-derived value, but is not guaranteed
	 * to do so in the future. If you need a unique timestamp, see {@link IDGenerator#generateTimestamp()}.
	 * @return a unique number
	 * @see IDGenerator#generateTimestamp()
	 */
	public synchronized static long generate() {

		// Get current time and check if it's newer than what came before
		long id = System.currentTimeMillis();
		if (id <= _lastID)
			id = ++_lastID;
		
		// Log the generated ID
		log.debug("Generated " + Long.toHexString(id));

		// Return the id
		return id;
	}
	
	/**
	 * Generates a unique timestamp. This timestamp is not guarnteed to be exactly accurate, since if this
	 * call returns the same value as the last call, it is incremented by 1.
	 * @return a 32-bit UNIX timestamp
	 */
	public synchronized static long generateTimestamp() {
	   return generate();
	}
}