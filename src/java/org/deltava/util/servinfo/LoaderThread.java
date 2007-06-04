// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.servinfo;

/**
 * A bean to store information about a thread downloading ServInfo data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LoaderThread {

	private long _startTime;
	private Thread _loader;
	private final Throwable _stackInfo = new Throwable("CallerStackTrace");

	/**
	 * Initializes the bean.
	 * @param t the loader thread
	 */
	LoaderThread(Thread t) {
		super();
		_loader = t;
		_startTime = System.currentTimeMillis();
	}

	/**
	 * Returns the loader thread.
	 * @return the thread
	 */
	public Thread getThread() {
		return _loader;
	}

	/**
	 * Returns the amount of time the loader has been running.
	 * @return the number of milliseconds since the loader started
	 */
	public long getRunTime() {
		return System.currentTimeMillis() - _startTime;
	}
	
	/**
	 * Returns the calling thread's stack trace.
	 * @return a Throwable with the stack trace
	 * @see LoaderThread#getLoaderStackInfo()
	 */
	public Throwable getStackInfo() {
		return _stackInfo;
	}
	
	/**
	 * Returns the loader thread's stack trace.
	 * @return a Throwable with the stack trace
	 * @see LoaderThread#getStackInfo()
	 */
	public Throwable getLoaderStackInfo() {
		Throwable t = new Throwable(_loader.getName() + "StackTrace");
		t.setStackTrace(_loader.getStackTrace());
		return t;
	}
	
	/**
	 * Sets the calling thread's stack trace.
	 * @param stack an array of StackTraceElements
	 */
	public void setStack(StackTraceElement[] stack) {
		_stackInfo.setStackTrace(stack);
	}
	
	public int hashCode() {
		return _loader.getName().hashCode();
	}
	
	/**
	 * Returns the thread name.
	 */
	public String toString() {
		return _loader.getName();
	}
}