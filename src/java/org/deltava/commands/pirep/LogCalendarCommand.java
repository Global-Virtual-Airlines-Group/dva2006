// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display a pilot's log book in a Calendar. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LogCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
        // Determine who to display
        int id = ctx.getID();
        if ((id == 0) && ctx.isAuthenticated())
        	id = ctx.getUser().getID();
		
		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		try {
			Connection con = ctx.getConnection();
			
            // Get the pilot profile
            GetPilot dao = new GetPilot(con);
            ctx.setAttribute("pilot", dao.get(id), REQUEST);
            
            // Get the flight reports
            GetFlightReports prdao = new GetFlightReports(con);
            Collection<FlightReport> pireps = prdao.getLogbookCalendar(id, cctx.getStartDate(), cctx.getDays());
            prdao.getCaptEQType(pireps);
			ctx.setAttribute("pireps", pireps, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/pilot/logCalendarW.jsp" : "/jsp/pilot/logCalendarM.jsp");
		result.setSuccess(true);
	}
}