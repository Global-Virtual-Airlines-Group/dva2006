// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to delete ACARS data from a Flight Report.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class ACARSDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports dao = new GetFlightReports(con);
			FlightReport fr = dao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Check our Access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete PIREP");
			
			// Delete the PIREP data
			if (fr.hasAttribute(FlightReport.ATTR_ACARS)) {
				fr.setAttribute(FlightReport.ATTR_ACARS, false);
				
				// Start the transaction
				ctx.startTX();
				
				// Delete the ACARS data
				SetACARSLog awdao = new SetACARSLog(con);
				awdao.deleteInfo(fr.getDatabaseID(FlightReport.DBID_ACARS));
				
				// Write the PIREP
				SetFlightReport wdao = new SetFlightReport(con);
				wdao.deleteACARS(fr.getID());
				wdao.write(fr);
				
				// Commit the transaction
				ctx.commitTX();
			}
			
			// Save the PIREP in the request
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("isACARSDeleted", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}