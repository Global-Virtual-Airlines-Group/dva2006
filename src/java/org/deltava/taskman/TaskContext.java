// Copyright 2007, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.Date;

import org.deltava.beans.Pilot;

/**
 * The execution context for a scheduled task.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

public class TaskContext extends org.deltava.jdbc.ConnectionContext {
	
	private Pilot _user;
	private Date _lastRun;

	/**
	 * Initializes the task context.
	 */
	TaskContext() {
		super();
	}
	
	/**
	 * Returns the date/time the Task was previously executed.
	 * @return the last execution date/time, or null if never
	 */
	public Date getLastRun() {
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
	public void setLastRun(Date lastRun) {
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