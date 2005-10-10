// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.tasks;

import org.deltava.taskman.Task;

/**
 * An Scheduled Task to periodically run a full JVM garbage collection.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GarbageCollectorTask extends Task {

	/**
	 * Creates the Scheduled Task.
	 */
	public GarbageCollectorTask() {
		super("Garbage Collector", GarbageCollectorTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {
		System.gc();
	}
}