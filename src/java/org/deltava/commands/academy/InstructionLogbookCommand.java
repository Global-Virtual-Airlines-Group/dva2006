// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.InstructionFlight;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Instruction logbooks.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionLogbookCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);

		// Get the user ID to check
		int id = ctx.getID();
		if ((id == 0) || (!ctx.isUserInRole("HR")))
			id = ctx.getUser().getID();

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the flights
			GetAcademyCalendar dao = new GetAcademyCalendar(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Collection<InstructionFlight> flights = dao.getFlightCalendar(null, 0, id); 
			vc.setResults(flights);
			
			// Load the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<InstructionFlight> i = flights.iterator(); i.hasNext(); ) {
				InstructionFlight flight = i.next();
				IDs.add(new Integer(flight.getInstructorID()));
				IDs.add(new Integer(flight.getPilotID()));
			}
			
			// Load the Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/insLogbook.jsp");
		result.setSuccess(true);
	}
}