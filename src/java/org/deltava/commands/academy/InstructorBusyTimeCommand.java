// Copyright 2007, 2008, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

/**
 * A Web Site Command to list busy time for a Flight Academy Instructor.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class InstructorBusyTimeCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
			Collection<InstructionBusy> busyTime = dao.getBusyCalendar(ctx.getID(), cctx.getRange());
			
			// Calculcate access rights
			Map<InstructionBusy, BusyTimeAccessControl> accessMap = new LinkedHashMap<InstructionBusy, BusyTimeAccessControl>();
			for (InstructionBusy ib : busyTime) {
				BusyTimeAccessControl access = new BusyTimeAccessControl(ctx, ib);
				access.validate();
				accessMap.put(ib, access);
			}
			
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Get the Flight Instructors
			GetUserData uddao = new GetUserData(con);
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Collection<Pilot> instructors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			for (AirlineInformation ai : uddao.getAirlines(true).values())
				instructors.addAll(pdao.getByRole("Instructor", ai.getDB()));

			// Load the Pilots
			Collection<Integer> pilotIDs = busyTime.stream().map(InstructionBusy::getID).collect(Collectors.toSet());
			ctx.setAttribute("pilots", pdao.get(uddao.get(pilotIDs)), REQUEST);
			ctx.setAttribute("instructors", instructors, REQUEST);
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