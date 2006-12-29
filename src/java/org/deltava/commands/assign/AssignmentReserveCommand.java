// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reserve a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentReserveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get Command Results
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Assignment
			GetAssignment dao = new GetAssignment(con);
			AssignmentInfo assign = dao.get(ctx.getID());
			if (assign == null)
				throw notFoundException("Invalid Flight Assignment - " + ctx.getID());

			// Check if we have any other open flight assignments
			boolean hasOpen = false;
			List<AssignmentInfo> assignments = dao.getByPilot(ctx.getUser().getID());
			for (Iterator<AssignmentInfo> i = assignments.iterator(); i.hasNext() && !hasOpen;) {
				AssignmentInfo a = i.next();
				hasOpen = hasOpen || (a.getStatus() == AssignmentInfo.RESERVED);
			}

			// If we have an open assignment, abort
			if (hasOpen) {
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
			for (Iterator i = assign.getAssignments().iterator(); i.hasNext();) {
				AssignmentLeg leg = (AssignmentLeg) i.next();

				// Create a draft PIREP from the assignment leg
				FlightReport fr = new FlightReport(leg);
				fr.setRank(ctx.getUser().getRank());
				fr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
				fr.setDatabaseID(FlightReport.DBID_ASSIGN, assign.getID());
				fr.setEquipmentType(assign.getEquipmentType());
				fr.setDate(new Date());

				// Save the flight report
				fwdao.write(fr);
				assign.addFlight(fr);
			}
			
			// Commit the transaction
			ctx.commitTX();

			// Save the assignment in the request
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
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}