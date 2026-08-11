// Copyright 2007, 2011, 2016, 2021, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;
import java.time.Instant;

import org.apache.logging.log4j.Level;

import org.deltava.beans.*;
import org.deltava.util.system.SystemData;

/**
 * The execution context for a scheduled task.
 * @author Luke
 * @version 12.5
 * @since 1.0
 */

public class TaskContext extends org.deltava.jdbc.ConnectionContext {
	
	private Pilot _user;
	private Instant _lastRun;
	private final TaskLog _log;

	/**
	 * Initializes the task context.
	 * @param log the TaskLog
	 */
	TaskContext(TaskLog log) {
		super();
		_log = log;
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
	
	public Collection<LogEntry> getLogEntries() {
		return Collections.unmodifiableCollection(_log.getEntries());
	}
	
	/**
	 * Logs a message to the Task logger.
	 * @param l the Log4J severity Level
	 * @param fmt the format string
	 * @param args the arguments
	 * @see String#format(String, Object...)
	 */
	public void log(Level l, String fmt, Object... args) {
		_log.log(l, fmt, args);
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