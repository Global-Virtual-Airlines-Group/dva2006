// Copyright 2005, 2006, 2009, 2010, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reserve a Flight Assignment.
 * @author Luke
 * @version 8.3
 * @since 1.0
 */

public class AssignmentReserveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Assignment
			GetAssignment dao = new GetAssignment(con);
			AssignmentInfo assign = dao.get(ctx.getID());
			if (assign == null)
				throw notFoundException("Invalid Flight Assignment - " + ctx.getID());

			// If we have an open assignment, abort
			List<AssignmentInfo> assignments = dao.getByPilot(ctx.getUser().getID(), AssignmentStatus.RESERVED);
			if (!assignments.isEmpty()) {
				ctx.release();
				result.setURL("/jsp/assign/assignOpen.jsp");
				result.setSuccess(true);
				return;
			}

			// Calculate our access
			AssignmentAccessControl access = new AssignmentAccessControl(ctx, assign);
			access.validate();
			if (!access.getCanReserve())
				throw securityException("Cannot reserve Flight Assignment " + ctx.getID());
			
			// Start the transaction
			ctx.startTX();

			// Update the assignment
			SetAssignment wdao = new SetAssignment(con);
			wdao.assign(assign, ctx.getUser().getID(), SystemData.get("airline.db"));

			// Write the Flight Reports
			SetFlightReport fwdao = new SetFlightReport(con);
			for (AssignmentLeg leg : assign.getAssignments()) {
				FlightReport fr = new FlightReport(leg);
				fr.setRank(ctx.getUser().getRank());
				fr.setDatabaseID(DatabaseID.PILOT, ctx.getUser().getID());
				fr.setDatabaseID(DatabaseID.ASSIGN, assign.getID());
				fr.setEquipmentType(assign.getEquipmentType());
				fr.setDate(Instant.now());
				fwdao.write(fr);
				assign.addFlight(fr);
			}
			
			// Save the assignment in the request
			ctx.commitTX();
			ctx.setAttribute("assign", assign, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isReserve", Boolean.TRUE, REQUEST);
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}