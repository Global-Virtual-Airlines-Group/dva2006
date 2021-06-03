// Copyright 2008, 2009, 2010, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to build a flight assignment that consists of a single leg selected at random from the last
 * Airport the Pilot completed a flight to in the selected aircraft.
 * @author Luke
 * @version 10.0
 * @since 2.2
 */

public class SingleAssignmentBuildCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();

		// Search for the entry in the session - if not found, go back to the search page
		AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("assign");
		if ((info == null) || (info.getAssignments().isEmpty())) {
			result.setURL("singleassign.do");
			result.setType(ResultType.REDIRECT);
			return;
		}
		
		// Update the flight assignment
		info.setAssignDate(Instant.now());
		info.setPilotID(ctx.getUser());
		info.setStatus(AssignmentStatus.RESERVED);
		info.setRandom(true);
		info.setPurgeable(true);

		try {
			Connection con = ctx.getConnection();

			// Start a transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, ctx.getDB());
			awdao.assign(info, info.getPilotID(), ctx.getDB());

			// Write the PIREPs to the database
			ctx.setAttribute("pirepsWritten", Boolean.TRUE, REQUEST);
			SetFlightReport pwdao = new SetFlightReport(con);
			for (FlightReport fr : info.getFlights()) {
				fr.setDate(info.getAssignDate());
				fr.setRank(ctx.getUser().getRank());
				fr.setDatabaseID(DatabaseID.PILOT, ctx.getUser().getID());
				fr.setDatabaseID(DatabaseID.ASSIGN, info.getID());
				pwdao.write(fr);
			}

	        ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Write status variables
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
	    ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
	    ctx.setAttribute("assign", info, REQUEST);
	    ctx.getSession().removeAttribute("assign");

		// Forward to the JSP
	    result.setURL("/jsp/assign/assignUpdate.jsp");
	    result.setType(ResultType.REQREDIRECT);
	    result.setSuccess(true);
	}
}