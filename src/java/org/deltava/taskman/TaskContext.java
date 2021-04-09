// Copyright 2007, 2011, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.time.Instant;

import org.deltava.beans.Pilot;
import org.deltava.util.system.SystemData;

/**
 * The execution context for a scheduled task.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class TaskContext extends org.deltava.jdbc.ConnectionContext {
	
	private Pilot _user;
	private Instant _lastRun;

	/**
	 * Initializes the task context.
	 */
	TaskContext() {
		super();
		setDB(SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the date/time the Task was previously executed.
	 * @return the last execution date/time, or null if never
	 */
	public Instant getLastRun() {
		return _lastRun;
	}
	
	/**
	 * Returns the User the Task will be executing as.
	 * @return a Pilot, or null if the system
	 */
	public Pilot getUser() {
		return _user;
	}
	
	/**
	 * Updates the date/time the Task was previously executed.
	 * @param lastRun the last execution date/time, or null if never
	 */
	public void setLastRun(Instant lastRun) {
		_lastRun = lastRun;
	}

	/**
	 * Updates the User the Task will be executing as.
	 * @param usr a Pilot, or null if the system
	 */	
	public void setUser(Pilot usr) {
		_user = usr;
	}
}