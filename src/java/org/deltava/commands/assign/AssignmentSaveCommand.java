// Copyright 2005, 2006, 2008, 2009, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to save a Flight Assignment.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class AssignmentSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get command results
		CommandResult result = ctx.getResult();

		// Check for the flight assignment
		AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("buildAssign");
		if (info == null) {
			ctx.setMessage("Flight Assignment data not found - Session Timeout?");
			result.setURL("/jsp/schedule/findAflight.jsp");
			result.setType(ResultType.REQREDIRECT);
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();

			// Start the transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, ctx.getDB());

			// If the assignment has a pilot linked with it, write the draft PIREPs
			if ((info.getStatus() == AssignmentStatus.RESERVED) && (info.getPilotID() != 0)) {
				Instant now = Instant.now();
				info.setAssignDate(now);
				awdao.assign(info, info.getPilotID(), ctx.getDB());

				// Write the PIREPs to the database
				ctx.setAttribute("pirepsWritten", Boolean.TRUE, REQUEST);
				SetFlightReport pwdao = new SetFlightReport(con);
				for (FlightReport fr : info.getFlights()) {
					fr.setDate(now);
					fr.setRank(ctx.getUser().getRank());
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Assigned from Schedule Search");
					pwdao.write(fr);
				}
			}

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set attributes and clean up session
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("assign", info, REQUEST);
		ctx.getSession().removeAttribute("buildAssign");
		ctx.getSession().removeAttribute("fafCriteria");

		// Redirect to the update page
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}