// Copyright 2006, 2007, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.BusyTimeAccessControl;

/**
 * A Web Site Command to display the Flight Academy Instruction Calendar.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class InstructionCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Calculate busy create access rights
		BusyTimeAccessControl ac = new BusyTimeAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);

		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Calendar
			GetAcademyCalendar dao = new GetAcademyCalendar(con);
			Collection<InstructorBean> entries = new ArrayList<InstructorBean>(dao.getSessionCalendar(ctx.getID(), cctx.getRange()));
			Collection<InstructionBusy> busyTime = dao.getBusyCalendar(ctx.getID(), cctx.getRange()); 
			entries.addAll(busyTime);
			ctx.setAttribute("sessions", entries, REQUEST);
			
			// Calculcate access rights
			Map<InstructionBusy, BusyTimeAccessControl> accessMap = new LinkedHashMap<InstructionBusy, BusyTimeAccessControl>();
			for (Iterator<InstructionBusy> i = busyTime.iterator(); i.hasNext(); ) {
				InstructionBusy ib = i.next();
				BusyTimeAccessControl access = new BusyTimeAccessControl(ctx, ib);
				access.validate();
				accessMap.put(ib, access);
			}
			
			// Save busy time
			ctx.setAttribute("accessMap", accessMap, REQUEST);

			// Get the Flight Instructors
			GetUserData uddao = new GetUserData(con);
			GetPilotDirectory prdao = new GetPilotDirectory(con);
			Collection<Pilot> instructors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			for (AirlineInformation ai : uddao.getAirlines(true).values())
				instructors.addAll(prdao.getByRole("Instructor", ai.getDB()));
			
			ctx.setAttribute("instructors", instructors, REQUEST);
			
			// Get the Pilot IDs from the sessions
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<? extends InstructorBean> i = entries.iterator(); i.hasNext(); ) {
				InstructorBean s = i.next();
				pilotIDs.add(Integer.valueOf(s.getInstructorID()));
				if (s instanceof InstructionSession)
					pilotIDs.add(Integer.valueOf(((InstructionSession) s).getPilotID()));
			}
			
			// Load the Pilots
			ctx.setAttribute("pilots", prdao.get(uddao.get(pilotIDs)), REQUEST);
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
		result.setURL((cctx.getDays() == 7) ? "/jsp/academy/calendarW.jsp" : "/jsp/academy/calendarM.jsp");
		result.setSuccess(true);
	}
}