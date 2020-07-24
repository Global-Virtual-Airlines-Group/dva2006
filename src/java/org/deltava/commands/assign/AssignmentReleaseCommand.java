// Copyright 2005, 2006, 2007, 2009, 2010, 2016, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to release a Flight Assignment.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AssignmentReleaseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Assignment
			GetAssignment dao = new GetAssignment(con);
			AssignmentInfo assign = dao.get(ctx.getID());
			if (assign == null)
				throw notFoundException("Invalid Flight Assignment - " + ctx.getID());

			// Calculate our access
			AssignmentAccessControl access = new AssignmentAccessControl(ctx, assign);
			access.validate();
			if (!access.getCanRelease())
				throw securityException("Cannot release Flight Assignment " + ctx.getID());

			// Get the Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> pireps = frdao.getByAssignment(ctx.getID(), SystemData.get("airline.db"));

			// Delete PIREPs in draft status, and remove the Assignment ID for the others
			Collection<FlightReport> remainingFlights = pireps.stream().filter(fr -> (fr.getStatus() != FlightStatus.DRAFT)).collect(Collectors.toList());

			// Save the totals
			ctx.setAttribute("flightsDeleted", Integer.valueOf(pireps.size() - remainingFlights.size()), REQUEST);
			ctx.setAttribute("flightsUpdated",Integer.valueOf(remainingFlights.size()), REQUEST);
			
			// Start transaction
			ctx.startTX();
			
			// Update history for remaining flights
			if (!remainingFlights.isEmpty()) {
				SetFlightReport frwdao = new SetFlightReport(con);
				for (FlightReport fr : remainingFlights) {
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Released Flight Assignment");
					frwdao.write(fr);
				}
			}

			// Delete or release the Assignment
			SetAssignment wdao = new SetAssignment(con);
			if (assign.isRepeating()) {
				wdao.reset(assign);
				ctx.setAttribute("isRelease", Boolean.TRUE, REQUEST);
			} else {
				wdao.delete(assign);
				ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the pilot
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}