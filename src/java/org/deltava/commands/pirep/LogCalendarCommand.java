// Copyright 2008, 2010, 2016, 2017, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display a Pilot's log book in a Calendar. 
 * @author Luke
 * @version 10.3
 * @since 2.2
 */

public class LogCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
            Collection<FlightReport> pireps = prdao.getLogbookCalendar(id, ctx.getDB(), cctx.getStartTime(), cctx.getDays()).stream().filter(fr -> (fr.getStatus() != FlightStatus.DRAFT)).collect(Collectors.toList());
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