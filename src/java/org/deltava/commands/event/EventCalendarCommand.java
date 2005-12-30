// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display the Online Event calendar.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventCalendarCommand extends AbstractCommand {
	
	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");
	private static final List TYPE_OPTIONS = ComboUtils.fromArray(new String[] {"Month", "Week"}, new String[] { "31", "7"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the number of days and start date
		int days = Integer.parseInt((String) ctx.getCmdParameter(OPERATION, "7"));
		Calendar startDate = Calendar.getInstance();
		try {
			startDate.setTime(_df.parse(ctx.getParameter("startDate")));
		} catch (ParseException pe) {
			// empty
		}

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the events
			GetEvent dao = new GetEvent(con);
			ctx.setAttribute("events", dao.getEvents(startDate.getTime(), days), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the calendar options
		ctx.setAttribute("startDate", startDate.getTime(), REQUEST);
		ctx.setAttribute("typeOptions", TYPE_OPTIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((days == 7) ? "/jsp/event/calendarW.jsp" : "/jsp/event/calendarM.jsp");
		result.setSuccess(true);
	}
}