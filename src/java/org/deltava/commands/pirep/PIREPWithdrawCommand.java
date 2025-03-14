// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.time.*;
import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to withdraw a submitted Flight Report.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class PIREPWithdrawCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports dao = new GetFlightReports(con);
			FlightReport fr = dao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanWithdraw())
				throw securityException("Cannot withdraw Flight Report " + fr.getID());
			
			// Update the flight
			Duration d = Duration.between(fr.getSubmittedOn(), Instant.now());
			fr.setStatus(FlightStatus.DRAFT);
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Withdrew submitted Flight Report after %d minutes", Long.valueOf(d.toMinutes())));
			
			// Save in the database
			ctx.startTX();
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.withdraw(fr, ctx.getDB());
			frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
			frwdao.deleteElite(fr);
			ctx.commitTX();
			
			// Save in request
			ctx.setAttribute("isWithdraw", Boolean.TRUE, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
		} catch (DAOException de) {
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