// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A bean to store System Log entries. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LogEntry extends DatabaseBean implements ViewEntry {
   
   public static final int DEBUG = 1;
   public static final int INFO = 2;
   public static final int WARN = 3;
   public static final int ERROR = 4;
   public static final int FATAL = 5;
   
   /**
    * Priority names.
    */
   public static final String[] PRIORITY = {"", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

	private Date _dt;
	private int _priority = INFO;
	private String _className;
	private String _msg;
	private String _err;
	
	/**
	 * Creates a new Log Entry.
	 * @param dt the date/time of the entry
	 * @see LogEntry#getDate()
	 */
	public LogEntry(Date dt) {
		super();
		_dt = dt;
	}
	
	/**
	 * Returns the entry priority. Since Log4J uses 
	 * @return the priority code
	 * @see LogEntry#setPriority(int)
	 */
	public int getPriority() {
		return _priority;
	}
	
	/**
	 * Returns the log entry date.
	 * @return the date/time the entry was created
	 * @see LogEntry#LogEntry(Date)
	 */
	public Date getDate() {
		return _dt;
	}
	
	/**
	 * Returns the logger class name.
	 * @return the logger class name
	 * @see LogEntry#setClassName(String)
	 * @see LogEntry#getName()
	 */
	public String getClassName() {
		return _className;
	}
	
	/**
	 * Returns the last part of the logger class name.
	 * @return the class name without package data
	 * @see LogEntry#getClassName()
	 */
	public String getName() {
		return _className.substring(_className.lastIndexOf('.') + 1);
	}
	
	/**
	 * Returns the log message.
	 * @return the message
	 * @see LogEntry#setMessage(String)
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns the stack trace from any error.
	 * @return the stack trace
	 * @see LogEntry#setError(String)
	 */
	public String getError() {
		return _err;
	}
	
	/**
	 * Sets the priority for this log entry. Since Log4J priority codes are multiples of 10,000, the priority
	 * code is divided by 10,000 to get the new priority.
	 * @param priority the priority code
	 * @throws IllegalArgumentException if priority is negative or invalid
	 * @see org.apache.log4j.Priority
	 */
	public void setPriority(int priority) {
	   priority = (priority > 9999) ? (priority /= 10000) : priority;
	   if ((priority < 1) || (priority >= PRIORITY.length))
	      throw new IllegalArgumentException("Invalid Priority code - " + priority);
	   
	   _priority = priority;
	}
	
	/**
	 * Sets the logger class name.
	 * @param cName the class name
	 * @throws NullPointerException if cName is null
	 * @see LogEntry#getClassName()
	 */
	public void setClassName(String cName) {
	   _className = cName.trim();
	}
	
	/**
	 * Sets the log message.
	 * @param msg the log message
	 * @see LogEntry#getMessage()
	 */
	public void setMessage(String msg) {
	   _msg = msg;
	}
	
	/**
	 * Sets the stack trace for an error.
	 * @param err the stack trace
	 * @see LogEntry#getError()
	 */
	public void setError(String err) {
	   _err = err;
	}

	/**
	 * Compares two Log Entries by comparing their dates.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		LogEntry le2 = (LogEntry) o2;
		return _dt.compareTo(le2.getDate());
	}
	
	/**
	 * Returns the CSS row class name if displaying in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
	   final String[] ROW_CLASS = {null, "opt2", null, "warn", "err", "err"};
		return ROW_CLASS[_priority];
	}
}