// Copyright 2006, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.InstructionFlight;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display Instruction logbooks.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class InstructionLogbookCommand extends AbstractViewCommand {
	
	private static final ComboAlias ALL = ComboUtils.fromString("All Instructors", "0x0");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);

		// Get the user ID to check
		int id = ctx.getID();
		if ((id == 0) && (!ctx.isUserInRole("HR")))
			id = ctx.getUser().getID();

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the flights
			GetAcademyCalendar dao = new GetAcademyCalendar(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Collection<InstructionFlight> flights = dao.getFlightCalendar(id, null); 
			vc.setResults(flights);
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(Integer.valueOf(ctx.getID()));
			for (InstructionFlight flight : flights) {
				IDs.add(Integer.valueOf(flight.getInstructorID()));
				IDs.add(Integer.valueOf(flight.getPilotID()));
			}
			
			// Load the Pilot beans
			GetUserData uddao = new GetUserData(con);
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Map<Integer, Pilot> pilots = pdao.get(uddao.get(IDs)); 
			ctx.setAttribute("pilots", pilots, REQUEST);
			Pilot ins = pilots.get(Integer.valueOf(ctx.getID()));
			ctx.setAttribute("ins", ins, REQUEST);
			
			// Load the instructor list
			Collection<Pilot> instructors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			for (AirlineInformation ai : uddao.getAirlines(true).values())
				instructors.addAll(pdao.getByRole("Instructor", ai.getDB()));
			
			// Sort and add
			List<ComboAlias> insList = new ArrayList<ComboAlias>(instructors);
			insList.add(0, ALL);
			ctx.setAttribute("instructors", insList, REQUEST);
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