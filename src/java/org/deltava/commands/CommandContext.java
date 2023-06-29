// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2015, 2016, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import javax.servlet.http.*;

import org.deltava.util.StringUtils;

/**
 * A class for storing run-time data needed for Command invocations. This class handles reserving and releasing JDBC
 * Connections, since by doing so we can easily return connections back to the pool in a <b>finally</b> block without
 * nasty scope issues.
 * @author Luke
 * @version 11.0
 * @since 1.0
 * @see Command
 */

public class CommandContext extends HTTPContext {
	
	public static final String AUTH_COOKIE_NAME = "authToken";

	public static final String USRLISTENER_ATTR_NAME = "userSerializeListener";
	public static final String INVALIDREQ_ATTR_NAME = "requestMapInvalid";
	public static final String SYSMSG_ATTR_NAME ="system_message";

	private final CommandResult _result = new CommandResult(null);

	/**
	 * Creates a new Command context from an HTTP Servlet Request/Resposne pair.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 */
	public CommandContext(HttpServletRequest req, HttpServletResponse rsp) {
		super(req, rsp);
	}

	@Override
	public long release() {
		long time = super.release();
		_result.setBackEndTime(time);
		return time;
	}

	/**
	 * Returns the Command Result object.
	 * @return the results of the command invocation
	 */
	public CommandResult getResult() {
		return _result;
	}

	/**
	 * Passes a system message into the response.
	 * @param msg the System Message
	 */
	public void setMessage(String msg) {
		setAttribute(SYSMSG_ATTR_NAME, msg, Command.Scope.REQ);
	}

	/**
	 * Returns a document ID as an integer.
	 * @return the datbase ID, or 0 if not found
	 * @throws CommandException if the ID cannot be parsed into a number
	 */
	public int getID() throws CommandException {
		Object obj = getCmdParameter(Command.ID, Integer.valueOf(0));
		if (obj instanceof Integer i) return i.intValue();
		if (obj == null) throw new CommandException("Invalid Database ID - null", false) {{ setStatusCode(400); }};
		
		// Try and convert into an integer
		String s = String.valueOf(obj);
		try {
			return Integer.decode(s).intValue();
		} catch (Exception e) {
			throw new CommandException(String.format("Invalid Database ID - %s", StringUtils.stripInlineHTML(s)), false) {{ setStatusCode(400); }};
		}
	}
}