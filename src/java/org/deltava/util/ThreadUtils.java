// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * A utility class to handle Thread operations.
 * @author Luke
 * @version 2.3
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
	 * A null-safe way to determine if a ThreadGroup is alive. 
	 * @param tg the ThreadGroup
	 * @return TRUE if the ThreadGroup is alive, otherwise FALSE 
	 */
	public static boolean isAlive(ThreadGroup tg) {
		return ((tg != null) && !tg.isDestroyed());
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
	 * A null-safe way of killing all Thread in a ThreadGroup that swallows thread lifecycle exceptions. 
	 * @param tg the ThreadGroup to kill
	 * @param waitTime the time to wait for the thread to die in milliseconds
	 */
	public static void kill(ThreadGroup tg, long waitTime) {
		if (isAlive(tg))
			tg.interrupt();
		
		int totalTime = 0;
		while (isAlive(tg) && (totalTime < waitTime)) {
			sleep(125);
			totalTime += 125;
		}
	}
	
	/**
	 * Causes a thread to sleep, swallowing exceptions.
	 * @param sleepTime the duration to sleep for in milliseconds
	 */
	public static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			log.warn("Thread [" + Thread.currentThread().getName() + "] interrupted");
		}
	}

	/**
	 * Waits for a running Thread to complete.
	 * @param t the Thread
	 * @param maxTime the maximum number of milliseconds to wait
	 */
	public static void waitFor(Thread t, long maxTime) {
		int timeElapsed = 0;
		while (isAlive(t) && (timeElapsed < maxTime)) {
			sleep(150);
			timeElapsed += 150;
		}
	}
	
	/**
	 * Waits for a Thread pool to complete.
	 * @param tPool a Collection of Threads
	 */
	public static void waitOnPool(Collection<? extends Thread> tPool) {
		final List<Thread> threadPool = new ArrayList<Thread>(tPool);
		do {
			sleep(125);
			for (Iterator<? extends Thread> i = threadPool.iterator(); i.hasNext(); ) {
				Thread worker = i.next();
				if (!worker.isAlive())
					i.remove();
			}
		} while (!threadPool.isEmpty() && (!Thread.currentThread().isInterrupted()));
		
		// If we're interrupted, shut the threads down
		if (Thread.currentThread().isInterrupted()) {
			for (Iterator<? extends Thread> i = threadPool.iterator(); i.hasNext(); ) {
				Thread worker = i.next();
				if (worker.isAlive())
					worker.interrupt();
			}
		}
	}
}