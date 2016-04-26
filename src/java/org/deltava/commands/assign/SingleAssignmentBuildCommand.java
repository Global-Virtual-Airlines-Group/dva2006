// Copyright 2008, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a flight assignment that consists of a single leg selected at random from the last
 * Airport the Pilot completed a flight to in the selected aircraft.
 * @author Luke
 * @version 7.0
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
		info.setStatus(AssignmentInfo.RESERVED);
		info.setRandom(true);
		info.setPurgeable(true);

		try {
			Connection con = ctx.getConnection();

			// Start a transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, SystemData.get("airline.db"));
			awdao.assign(info, info.getPilotID(), SystemData.get("airline.db"));

			// Write the PIREPs to the database
			ctx.setAttribute("pirepsWritten", Boolean.TRUE, REQUEST);
			SetFlightReport pwdao = new SetFlightReport(con);
			for (Iterator<FlightReport> i = info.getFlights().iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				fr.setDate(info.getAssignDate());
				fr.setRank(ctx.getUser().getRank());
				fr.setDatabaseID(DatabaseID.PILOT, ctx.getUser().getID());
				pwdao.write(fr);
			}

			// Commit the transaction
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
