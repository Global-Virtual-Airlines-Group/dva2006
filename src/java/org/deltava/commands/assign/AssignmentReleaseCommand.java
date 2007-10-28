// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Web Site Command to release a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentReleaseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
			frdao.getCaptEQType(pireps);

			// Get the PIREP write DAO and init counters
			SetFlightReport fwdao = new SetFlightReport(con);
			int flightsDeleted = 0;
			int flightsUpdated = 0;
			
			// Start the database transaction
			ctx.startTX();

			// Delete PIREPs in draft status, and remove the Assignment ID for the others
			for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				if (fr.getStatus() == FlightReport.DRAFT) {
					fwdao.delete(fr.getID());
					flightsDeleted++;
				} else {
					fr.setDatabaseID(FlightReport.DBID_ASSIGN, 0);
					fwdao.write(fr);
					flightsUpdated++;
				}
			}

			// Save the totals
			ctx.setAttribute("flightsDeleted", Integer.valueOf(flightsDeleted), REQUEST);
			ctx.setAttribute("flightsUpdated",Integer.valueOf(flightsUpdated), REQUEST);

			// Delete or release the Assignment
			SetAssignment wdao = new SetAssignment(con);
			if (assign.isRepeating()) {
				wdao.reset(assign);
				ctx.setAttribute("isRelease", Boolean.TRUE, REQUEST);
			} else {
				wdao.delete(assign);
				ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
			}

			// Commit the transaction
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
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}