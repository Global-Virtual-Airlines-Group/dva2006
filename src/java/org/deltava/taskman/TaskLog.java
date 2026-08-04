// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.LogEntry;

import org.deltava.util.EnumUtils;

/**
 * A Scheduled Task logger. This passes messages to Log4j as well as maintaining its own list of log messages
 * which can be persisted to another store. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class TaskLog {
	
	private final Logger log;
	private final Collection<LogEntry> _entries = new ArrayList<LogEntry>();

	/**
	 * Creates the Task log.
	 * @param logClass the Logger class 
	 */
	public TaskLog(Class<?> logClass) {
		super();
		log = LogManager.getLogger(logClass);
	}
	
	private static LogEntry.Level getLevel(Level l) {
		return EnumUtils.parse(LogEntry.Level.class, l.name(), LogEntry.Level.INFO);
	}

	/**
	 * Logs a message.
	 * @param l the log Level
	 * @param fmt the format string
	 * @param data the format arguments
	 * @see String#format(String, Object...)
	 */
	public void log(Level l, String fmt, Object... data) {
		String msg = String.format(fmt, data);
		log.atLevel(l).log(msg);
		
		LogEntry le = new LogEntry(getLevel(l), msg);
		_entries.add(le);
	}
	
	/**
	 * Returns the log entries.
	 * @return a Collection of LogEntry beans
	 */
	public Collection<LogEntry> getEntries() {
		return _entries;
	}
}