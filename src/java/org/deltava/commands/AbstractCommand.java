// Copyright 2004, 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.text.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A class to support Web Site Commands.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public abstract class AbstractCommand implements Command {

	private String _id;
	private String _name;
	private final Collection<String> _roles = new HashSet<String>();

	/**
	 * Initializes this command.
	 * @param cmdName the name of the command
	 * @throws CommandException if the command name is null
	 * @throws IllegalStateException if the command has already been initialized
	 */
	public void init(String id, String cmdName) throws CommandException {
		if (_name != null)
			throw new IllegalStateException(_name + " Command already initialized");

		try {
			_id = id.trim();
			_name = cmdName.trim();
		} catch (NullPointerException npe) {
			throw new CommandException("Command ID/Name cannot be null");
		}
	}
	
	/**
	 * Helper method to generate a security exception.
	 * @param msg the exception message
	 * @return a CommandException
	 */
	protected CommandException securityException(String msg) {
		CommandException ce = new CommandException("Security Error - " + msg, false);
		ce.setForwardURL("/jsp/error/securityViolation.jsp");
		ce.setWarning(true);
		return ce;
	}
	
	/**
	 * Helper method to generate an &quot;item not found&quot; exception.
	 * @param msg the exception message
	 * @return a CommandException
	 */
	protected CommandException notFoundException(String msg) {
		return new CommandException(msg, false);
	}

	/**
	 * Returns the Command name.
	 * @return the name of the command
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * Returns the Command ID.
	 * @return the command ID
	 */
	public final String getID() {
		return _id;
	}

	/**
	 * Return the roles authorized to execute this command. If setRoles() has not been called, this will return an empty
	 * Set. Commands defined to be executed by all users should have a wildcard entry (*) as an authorized role.
	 * @return a Collection of role names
	 * @see AbstractCommand#setRoles(Collection)
	 */
	public final Collection<String> getRoles() {
		Collection<String> results = new HashSet<String>();
		if (_roles != null)
			results.addAll(_roles);
		
		return results;
	}

	/**
	 * Updates the roles authorized to execute this command. This will make a copy of the List object provided (ie.
	 * making it immutable) for security reasons.
	 * @param roles the List of role names
	 * @throws IllegalStateException if setRoles() has already been called
	 * @see AbstractCommand#getRoles()
	 */
	public final void setRoles(Collection<String> roles) {
		if (!_roles.isEmpty())
			throw new IllegalStateException("Roles for " + getName() + " already set");

		_roles.addAll(roles);
	}

	/**
	 * Parses one or two HTTP request parameters into a date/time value. The parameter name header is used to construct
	 * the request parameter names. If the request contains a parameter called &quot;$HDR$dateTime&quot;, then the
	 * parameter will be parsed as a date/time value. If the request contains two parameters named &quot;$HDR$date&quot;
	 * and &quot;$HDR$time&quot;, then they will be appended and parsed together. If only a single parameter is found,
	 * it is parsed appropriately.
	 * @param ctx the Command Context
	 * @param paramHdr the parameter name header
	 * @param dfmt the date format pattern
	 * @param tfmt time time format pattern
	 * @return a date/time value, or null if not found or unparseable
	 * @see DateFormat#parse(java.lang.String)
	 */
	protected Date parseDateTime(CommandContext ctx, String paramHdr, String dfmt, String tfmt) {
	   
	   Date dt = null;
		try {
			if (ctx.getParameter(paramHdr + "DateTime") != null) {
				DateFormat df = new SimpleDateFormat(dfmt + " " + tfmt);
				dt = df.parse(ctx.getParameter(paramHdr + "DateTime"));
			} else if ((ctx.getParameter(paramHdr + "Date") != null) && (ctx.getParameter(paramHdr + "Time") != null)) {
			   String timeValue = ctx.getParameter(paramHdr + "Time");
			   if (StringUtils.isEmpty(timeValue)) {
			      DateFormat tf = new SimpleDateFormat(tfmt); 
			      timeValue = tf.format(new Date(0));
			   }
			      
				DateFormat df = new SimpleDateFormat(dfmt + " " + tfmt);
				dt = df.parse(ctx.getParameter(paramHdr + "Date") + " " + timeValue);
			} else if (ctx.getParameter(paramHdr + "Date") != null) {
				DateFormat df = new SimpleDateFormat(dfmt);
				dt = df.parse(ctx.getParameter(paramHdr + "Date"));
			} else if (ctx.getParameter(paramHdr + "Time") != null) {
				DateFormat df = new SimpleDateFormat(tfmt);
				dt = df.parse(ctx.getParameter(paramHdr + "Time"));
			} else {
			   return null;
			}
		} catch (ParseException pe) {
		   return null;
		}
		
		// Convert from user's time zone, or default zone to the JVM's local zone
		TZInfo tz =  (ctx.getUser() == null) ? TZInfo.get(SystemData.get("time.timezone")) : ctx.getUser().getTZ();
		DateTime dtf = new DateTime(dt, tz);
		dtf.convertTo(TZInfo.local());
		return dtf.getDate();
	}

	/**
	 * Parses one or two HTTP request parameters into a date/time value, using the user's format patterns or
	 * the default format patterns if the user is not authenticated.
	 * @param ctx the Command Context
	 * @param paramHdr the parameter name header
	 * @return a date/time value, or null if not found or unparseable
	 * @see AbstractCommand#parseDateTime(CommandContext, String, String, String)
	 */
	protected Date parseDateTime(CommandContext ctx, String paramHdr) {
		String dfmt = (ctx.getUser() == null) ? SystemData.get("time.date_format") : ctx.getUser().getDateFormat();
		String tfmt = (ctx.getUser() == null) ? SystemData.get("time.time_format") : ctx.getUser().getTimeFormat();
		return parseDateTime(ctx, paramHdr, dfmt, tfmt);
	}
}