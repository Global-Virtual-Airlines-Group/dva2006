// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A unique ID generator for messaging purposes.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class IDGenerator {

	private static final AtomicLong _id = new AtomicLong(System.currentTimeMillis());

	/**
	 * Generates a unique ID. This currently returns a timestamp-derived value, but is not guaranteed to do so in the future.
	 * @return a unique number
	 */
	public static long generate() {
		long id = _id.incrementAndGet();
		return id;
	}
}