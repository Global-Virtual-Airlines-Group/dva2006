// Copyright 2007, 2009, 2016, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to release a held Flight Report.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class PIREPReleaseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Flight Report to modify
			GetFlightReports rdao = new GetFlightReports(con);
			FlightReport fr = rdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Flight Report Not Found");
			
			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			if (!access.getCanRelease())
				throw securityException("Cannot release Flight Report #" + ctx.getID());
			
			// Load the comments
			if (ctx.getParameter("dComments") != null)
				fr.setComments(ctx.getParameter("dComments"));
			
			// Add release notes
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Hold Released");
			
			// Get the write DAO and update/dispose of the PIREP
			ctx.startTX();
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(SystemData.get("airline.db"), null, fr, FlightStatus.SUBMITTED);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the Flight Report
		CommandResult result = ctx.getResult();
		result.setURL("pirep", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}