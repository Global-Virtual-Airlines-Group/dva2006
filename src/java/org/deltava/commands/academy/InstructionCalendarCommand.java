// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.InstructionSession;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the Flight Academy Instruction Calendar.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionCalendarCommand extends AbstractCalendarCommand {

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
			
			// Get the DAO and the Calendar
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Collection<InstructionSession> sessions = dao.getCalendar(cctx.getStartDate(), cctx.getDays(), 0);
			ctx.setAttribute("sessions", sessions, REQUEST);
			
			// Get the Pilot IDs from the sessions
			Set<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<InstructionSession> i = sessions.iterator(); i.hasNext(); ) {
				InstructionSession s = i.next();
				pilotIDs.add(new Integer(s.getPilotID()));
				pilotIDs.add(new Integer(s.getInstructorID()));
			}
			
			// Load the Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/academy/calendarW.jsp" : "/jsp/academy/calendarM.jsp");
		result.setSuccess(true);
	}
}