// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

import org.deltava.util.StringUtils;

/**
 * A record class to store internal log messages.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class LogEntry implements java.io.Serializable, Comparable<LogEntry> {
	
	private final Level _l;
	private final String _msg;
	private final Instant _createdOn = Instant.now();

	/**
	 * An enumeration of logging levels.
	 */
	public enum Level {
		DEBUG, INFO, WARN, ERROR
	}
	
	/**
	 * Creates the Log Entry.
	 * @param l the Level
	 * @param msg the message
	 */
	public LogEntry(Level l, String msg) {
		super();
		_l = l;
		_msg = msg;
	}
	
	/**
	 * Returns the log level.
	 * @return the Level
	 */
	public Level getLeve() {
		return _l;
	}
	
	/**
	 * Returns the log message.
	 * @return the message
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns the creation time of this log message.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(StringUtils.format(_createdOn, "MM/dd/yyyy HH:mm:ss"));
		buf.append(' ').append(_l.name());
		buf.append(' ').append(_msg);
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public int compareTo(LogEntry le2) {
		int tmpResult = _createdOn.compareTo(le2._createdOn);
		return (tmpResult == 0) ? _l.compareTo(le2._l) : tmpResult;
	}
}