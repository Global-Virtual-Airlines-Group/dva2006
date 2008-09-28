// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.util.Date;
import java.io.Serializable;

import org.deltava.beans.ViewEntry;
import org.deltava.commands.CommandResult;

/**
 * A bean to log Web Site Command invocations.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class CommandLog implements Serializable, Comparable<CommandLog>, ViewEntry {

	private Date _d;
	private String _cmdName;
	private String _remoteAddr;
	private String _remoteHost;
	private String _result;

	private int _pilotID;
	private boolean _success;
	private long _totalTime;
	private long _backEndTime;

	/**
	 * Creates a new Command log entry.
	 * @param d the date/time of the command invocation.
	 * @see CommandLog#getDate()
	 */
	public CommandLog(Date d) {
		super();
		_d = d;
	}

	/**
	 * Creates a new Command log entry.
	 * @param cmdName the Command name
	 * @param cr the CommandResult bean
	 */
	public CommandLog(String cmdName, CommandResult cr) {
		this(new Date());
		_cmdName = cmdName;
		if (cr != null) {
			_success = cr.getSuccess();
			_backEndTime = (int) cr.getBackEndTime();
			_totalTime = cr.getTime();
		}
	}

	/**
	 * Returns the date the Command was executed.
	 * @return the execution date/time
	 */
	public Date getDate() {
		return _d;
	}

	/**
	 * Returns the Command name.
	 * @return the Command name
	 * @see CommandLog#setName(String)
	 */
	public String getName() {
		return _cmdName;
	}

	/**
	 * Returns the IP address of the web user executing the Command.
	 * @return the IP address
	 * @see CommandLog#setRemoteAddr(String)
	 * @see CommandLog#getRemoteHost()
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	/**
	 * Returns the host name of the web user executing the Command.
	 * @return the host name
	 * @see CommandLog#setRemoteHost(String)
	 * @see CommandLog#getRemoteAddr()
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}

	/**
	 * Returns the command result data.
	 * @return the result data
	 * @see CommandLog#setResult(String)
	 */
	public String getResult() {
		return _result;
	}

	/**
	 * Returns the database ID of the Pilot executing the Command.
	 * @return the database ID
	 * @see CommandLog#setPilotID(int)
	 * @see org.deltava.beans.Pilot#getID()
	 */
	public int getPilotID() {
		return _pilotID;
	}

	/**
	 * Returns if the Command completed succesfully.
	 * @return TRUE if execution was successful, otherwise FALSE
	 * @see CommandLog#setSuccess(boolean)
	 */
	public boolean getSuccess() {
		return _success;
	}

	/**
	 * Returns the amount of time the database was in use.
	 * @return the time in milliseconds
	 * @see CommandLog#setBackEndTime(long)
	 * @see CommandLog#getTime()
	 */
	public long getBackEndTime() {
		return _backEndTime;
	}

	/**
	 * Returns the execution time.
	 * @return the time of milliseconds
	 * @see CommandLog#setTime(long)
	 * @see CommandLog#getBackEndTime()
	 */
	public long getTime() {
		return _totalTime;
	}

	/**
	 * Updates the Command name.
	 * @param cmdName the name
	 * @see CommandLog#getName()
	 */
	public void setName(String cmdName) {
		_cmdName = cmdName;
	}

	/**
	 * Updates the IP address of the user executing the Command.
	 * @param addr the IP address
	 * @see CommandLog#getRemoteAddr()
	 * @see CommandLog#setRemoteHost(String)
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}

	/**
	 * Updates the host name of the user executing the Command.
	 * @param hostName the host name
	 * @see CommandLog#getRemoteHost()
	 * @see CommandLog#setRemoteAddr(String)
	 */
	public void setRemoteHost(String hostName) {
		_remoteHost = hostName;
	}

	/**
	 * Updates the Command result data.
	 * @param msg the result data
	 * @see CommandLog#getResult()
	 */
	public void setResult(String msg) {
		_result = msg;
	}

	/**
	 * Updates the database ID of the user executing this Command.
	 * @param id the Pilot's database ID
	 * @see CommandLog#getPilotID()
	 * @see org.deltava.beans.Pilot#getID()
	 */
	public void setPilotID(int id) {
		_pilotID = id;
	}

	/**
	 * Marks wether this Command executed successfully.
	 * @param isOK TRUE if the Command completed successfully, otherwise FALSE
	 * @see CommandLog#getSuccess()
	 */
	public void setSuccess(boolean isOK) {
		_success = isOK;
	}

	/**
	 * Updates the time spent accessing the database.
	 * @param time the time in milliseconds
	 * @see CommandLog#getBackEndTime()
	 * @see CommandLog#setTime(long)
	 */
	public void setBackEndTime(long time) {
		_backEndTime = time;
	}

	/**
	 * Updates the total execution time.
	 * @param time the time in milliseconds
	 * @see CommandLog#getTime()
	 * @see CommandLog#setBackEndTime(long)
	 */
	public void setTime(long time) {
		_totalTime = time;
	}

	/**
	 * Compares two log entries by comparing their dates.
	 */
	public int compareTo(CommandLog cl2) {
		return _d.compareTo(cl2._d);
	}

	/**
	 * Returns the CSS table row class name.
	 * @return null or &quot;warn&quot; if execution not successful
	 * @see CommandLog#getSuccess()
	 */
	public String getRowClassName() {
		return _success ? null : "warn";
	}
}