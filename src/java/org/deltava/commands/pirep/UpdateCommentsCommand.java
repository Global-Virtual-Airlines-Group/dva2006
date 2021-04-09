// Copyright 2014, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to update Flight Report disposition comments. 
 * @author Luke
 * @version 10.0
 * @since 5.3
 */

public class UpdateCommentsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report ID - " + ctx.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanUpdateComments())
				throw securityException("Cannot update Flight Report comments");
			
			// Update the comments
			fr.setComments(ctx.getParameter("dComments"));
			
			// Save the flight
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.writeComments(fr);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Commmand
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", null, ctx.getID());
		result.setSuccess(true);
	}
}