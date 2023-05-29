// Copyright 2005, 2006, 2007, 2008, 2009, 2013, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.apache.logging.log4j.*;

import java.util.concurrent.Future;

/**
 * A utility class to handle Thread operations.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class ThreadUtils {
	
	private static final Logger log = LogManager.getLogger(ThreadUtils.class);

	// Singleton
	private ThreadUtils() {
		super();
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
				log.warn("Cannot kill thread [" + t.getName() + "] after " + waitTime + "ms");
			}
		}
	}
	
	/**
	 * A null-safe way of killing a number of threads that swallows thread lifecycle exceptions. 
	 * @param threads a Collection of Threads to kill
	 * @param waitTime the time to wait for the thread to die in milliseconds
	 */
	public static void kill(Collection<Thread> threads, long waitTime) {
		Collection<Thread> t2 = new ArrayList<Thread>(threads);
		t2.forEach(Thread::interrupt);
		int totalTime = 0;
		while ((t2.size() > 0) && (totalTime < waitTime)) {
			t2.removeIf(ThreadUtils::isAlive);
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
			threadPool.removeIf(w -> !w.isAlive());
		} while (!threadPool.isEmpty() && !Thread.currentThread().isInterrupted());
		
		// If we're interrupted, shut the threads down
		if (Thread.currentThread().isInterrupted())
			threadPool.forEach(w -> w.interrupt());
	}

	/**
	 * Waits for a collection of operations to complete. 
	 * @param futures a Collection of Futures
	 */
	public static void waitOnFutures(Collection<Future<?>> futures) {
		final Collection<Future<?>> fPool = new ArrayList<Future<?>>(futures);
		do {
			sleep(125);
			fPool.removeIf(f -> f.isDone());
		} while (!fPool.isEmpty() && (!Thread.currentThread().isInterrupted()));
	}
}