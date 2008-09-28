// Copyright 2004, 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;

/**
 * Returns the results of a web Command. All commands need to return an object of this class when completing their
 * execute() method, and based on the data contained within this class the controller servlet will perform further
 * processing.
 * @author Luke
 * @version 2.2
 * @since 1.0
 * @see Command#execute(CommandContext)
 */

public class CommandResult implements java.io.Serializable {
	
	private String _resultURL;
	private boolean _success;
	private long _executeTime;
	private long _backEndTime;
	private ResultType _resultCode = ResultType.FORWARD;
	private int _httpCode;
	private long _startTime;

	/**
	 * Create a new CommandResult forwarding the Controller to a new servlet.
	 * @param resultPage the resource name to forward to
	 */
	CommandResult(String resultPage) {
		super();
		_startTime = System.currentTimeMillis();
		_resultURL = resultPage;
	}

	/**
	 * Did the command complete successfully?
	 * @return TRUE if the command completed successfully, otherwise FALSE
	 * @see CommandResult#setSuccess(boolean)
	 */
	public boolean getSuccess() {
		return _success;
	}

	/**
	 * Return the execution time of the last command.
	 * @return the time the last command took to execute, in milliseconds
	 * @see CommandResult#setTime(long)
	 */
	public long getTime() {
		return _executeTime;
	}

	/**
	 * Return the back-end utilization time of the last command.
	 * @return the time the back-end was used, in milliseconds. This is useful for determining usage of shared back-end
	 *             resources, like JDBC or JNDI connections.
	 * @see CommandResult#setBackEndTime(long)
	 */
	public long getBackEndTime() {
		return _backEndTime;
	}

	/**
	 * Returns the URL the controller should forward to.
	 * @return the URL/resource to forward to
	 */
	public String getURL() {
		return _resultURL;
	}

	/**
	 * Returns the HTTP status code to return.
	 * @return the HTTP status code
	 * @see CommandResult#setHttpCode(int)
	 */
	public int getHttpCode() {
		return _httpCode;
	}

	/**
	 * The type of action the controller should perform next.
	 * @return the action type for the controller
	 */
	public ResultType getType() {
		return _resultCode;
	}

	/**
	 * A helper method to "stop the clock" for execution and setTime().
	 * @see CommandResult#setTime(long)
	 * @see CommandResult#getTime()
	 */
	public void complete() {
		setTime(System.currentTimeMillis() - _startTime);
	}

	/**
	 * Mark if the previous Command complated successfully
	 * @param success TRUE if the command completed successfully, otherwise FALSE
	 */
	public void setSuccess(boolean success) {
		_success = success;
	}

	/**
	 * Set the total execution time for this Command
	 * @param time the time in milliseconds
	 * @see CommandResult#getTime()
	 * @see CommandResult#complete()
	 */
	public void setTime(long time) {
		_executeTime = Math.max(0, time);
	}

	/**
	 * Set the total back-end usage time for this Command.
	 * @param time the back-end usage time in milliseconds
	 * @see CommandResult#getBackEndTime()
	 */
	public void setBackEndTime(long time) {
		_backEndTime = Math.max(0, time);
	}

	/**
	 * Updates the HTTP status code to return on completion.
	 * @param code the HTTP status code
	 * @throws IllegalStateException if result has not been set to RESULT_HTTPCODE
	 * @see CommandResult#getHttpCode()
	 */
	public void setHttpCode(int code) {
		if (_resultCode != ResultType.HTTPCODE)
			throw new IllegalStateException("Command Result must set HTTP code");

		_httpCode = code;
	}

	/**
	 * Set the URL to process next.
	 * @param url the URL/resource name
	 */
	public void setURL(String url) {
		_resultURL = url;
	}

	/**
	 * Sets the URL to process next, when executing a command with parameters.
	 * @param cmdName the command Name
	 * @param opName the optional operation name
	 * @param id the optional ID
	 */
	public void setURL(String cmdName, String opName, String id) {
		StringBuilder buf = new StringBuilder("/");
		buf.append(cmdName.toLowerCase());
		buf.append(".do");
		
		// Add in the operation and ID
		Map<String, String> params = new LinkedHashMap<String, String>();
		if (id != null) params.put("id", id);
		if (opName != null) params.put("op", opName);
		
		// Parse the maps
		if (params.size() > 0) {
		   buf.append('?');
		   for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
		      String pName = (String) i.next();
		      buf.append(pName);
		      buf.append('=');
		      buf.append(params.get(pName));
		      if (i.hasNext())
		         buf.append('&');
		   }
		}

		// Set the URL
		setURL(buf.toString());
	}
	
	/**
	 * Sets the URL to process next, when executing a command with parameters and a numeric ID.
	 * @param cmdName the command Name
	 * @param opName the optional operation name
	 * @param id the ID, which will be converted to hexadecimal and prepended with &quot;0x&quot;
	 */
	public void setURL(String cmdName, String opName, int id) {
		setURL(cmdName, opName, "0x" + Integer.toHexString(id));
	}
	
	/**
	 * Sets the result type of this command. This tells the controller servlet what kind of action to perform next on
	 * the URL.
	 * @param type the result type
	 * @throws IllegalArgumentException if resultCode is negative or not listed in RESULT
	 * @see ResultType
	 * @see CommandResult#getType()
	 */
	public void setType(ResultType type) {
		_resultCode = type;
	}
}