// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import org.apache.log4j.Logger;

/**
 * A utility class to handle Thread operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadUtils {
	
	private static final Logger log = Logger.getLogger(ThreadUtils.class);

	// Singleton
	private ThreadUtils() {
	}
	
	/**
	 * A null-safe way to determine if a Thread is alive. 
	 * @param t the Thread
	 * @return TRUE if the Thread is alive, otherwise FALSE 
	 */
	public static boolean isAlive(Thread t) {
		return ((t != null) && t.isAlive());
	}
	
	/**
	 * A null-safe way of killing a Thread that swallows thread lifecycle exceptions. 
	 * @param t the Thread to kill
	 * @param waitTime the time to wait for the thread to die in milliseconds
	 */
	public static void kill(Thread t, long waitTime) {
		if (isAlive(t)) {
			try {
				t.interrupt();
				t.join(waitTime);
			} catch (InterruptedException ie) {
				log.warn("Cannot kill thread [" + t.getName() + "]");
			}
		}
	}
	
	/**
	 * Causes a thread to sleep, swallowing exceptions
	 * @param sleepTime the duration to sleep for in milliseconds
	 */
	public static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
			log.warn("Thread [" + Thread.currentThread().getName() + "] interrupted");
		}
	}
}