// Copyright 2005, 2009, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A unique ID generator for messaging purposes.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class IDGenerator {

	private final AtomicLong _id = new AtomicLong(System.currentTimeMillis());

	/**
	 * Generates a unique ID. This currently returns a timestamp-derived value, but is not guaranteed to do so in the future.
	 * @return a unique number
	 */
	public long generate() {
		return _id.incrementAndGet();
	}
	
	/**
	 * Resets the value to the current timestamp.
	 */
	public void reset() {
		_id.set(System.currentTimeMillis());
	}
	
	/**
	 * Sets the value to use.
	 * @param value the value
	 */
	public void reset(long value) {
		_id.set(value);
	}
}