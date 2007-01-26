// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to list busy time for a Flight Academy Instructor.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructorBusyTimeCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Calculate create access rights
		BusyTimeAccessControl ac = new BusyTimeAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);

		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Calendar
			GetAcademyCalendar dao = new GetAcademyCalendar(con);
			Collection<InstructionBusy> busyTime = dao.getBusyCalendar(cctx.getStartDate(), cctx.getDays(), ctx.getID());
			
			// Calculcate access rights
			Map<InstructionBusy, BusyTimeAccessControl> accessMap = new LinkedHashMap<InstructionBusy, BusyTimeAccessControl>();
			for (Iterator<InstructionBusy> i = busyTime.iterator(); i.hasNext(); ) {
				InstructionBusy ib = i.next();
				BusyTimeAccessControl access = new BusyTimeAccessControl(ctx, ib);
				access.validate();
				accessMap.put(ib, access);
			}
			
			ctx.setAttribute("busyTime", accessMap.keySet(), REQUEST);
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Get the Flight Instructors
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			ctx.setAttribute("instructors", pdao.getByRole("Instructor", SystemData.get("airline.db")), REQUEST);

			// Get the Pilot IDs from the sessions
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<InstructionBusy> i = busyTime.iterator(); i.hasNext(); ) {
				InstructionBusy s = i.next();
				pilotIDs.add(new Integer(s.getID()));
			}

			// Load the Pilot IDs
			ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save pilot data
		ctx.setAttribute("user", ctx.getUser(), REQUEST);
		ctx.setAttribute("isMine", Boolean.valueOf(ctx.getID() == 0), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/academy/busyCalendarW.jsp" : "/jsp/academy/busyCalendarM.jsp");
		result.setSuccess(true);
	}
}