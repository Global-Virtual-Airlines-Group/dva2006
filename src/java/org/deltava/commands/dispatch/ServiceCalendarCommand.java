// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.DispatchScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchScheduleAccessControl;

/**
 * A Web Site Command to display the ACARS Dispatch service calendar.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class ServiceCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the entries
			GetDispatchCalendar dcdao = new GetDispatchCalendar(con);
			Collection<DispatchScheduleEntry> results = dcdao.getCalendar(cctx.getStartDate(), cctx.getDays(), ctx.getID());
			ctx.setAttribute("entries", results, REQUEST);
			
			// Save Access Rights
			Map<DispatchScheduleEntry, DispatchScheduleAccessControl> accessMap = new 
				HashMap<DispatchScheduleEntry, DispatchScheduleAccessControl>();
			
			// Get the Dispatcher IDs and calculate access
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<DispatchScheduleEntry> i = results.iterator(); i.hasNext(); ) {
				DispatchScheduleEntry e = i.next();
				IDs.add(new Integer(e.getAuthorID()));
				
				// Get access
				DispatchScheduleAccessControl access = new DispatchScheduleAccessControl(ctx, e);
				access.validate();
				accessMap.put(e, access);
			}
			
			// Save the access controllers
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Load the Dispatchers
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate our access to create new entries'
		DispatchScheduleAccessControl ac = new DispatchScheduleAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/dispatch/calendarW.jsp" : "/jsp/dispatch/calendarM.jsp");
		result.setSuccess(true);
	}
}