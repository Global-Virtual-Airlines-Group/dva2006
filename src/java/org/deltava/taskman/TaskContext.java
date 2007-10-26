// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import org.deltava.jdbc.ConnectionContext;

/**
 * The execution context for a scheduled task.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TaskContext extends ConnectionContext {

	/**
	 * Initializes the task context.
	 */
	TaskContext() {
		super();
	}
}