// Copyright 2014, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.event.Event;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update Online Events linked to a Flight Report. 
 * @author Luke
 * @version 10.2
 * @since 5.3
 */

public class UpdateEventCommand extends AbstractCommand {

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
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the Event
			int eventID = StringUtils.parse(ctx.getParameter("onlineEvent"), 0);
			GetEvent edao = new GetEvent(con);
			Event e = edao.get(eventID);
			if ((e == null) && (eventID != 0))
				throw notFoundException("Invalid Online Event - " + eventID);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanAdjustEvents())
				throw securityException("Cannot update Flight Report Online Event");
			
			// Update event
			fr.setDatabaseID(DatabaseID.EVENT, (e == null) ? 0 : e.getID());
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Updated Online Event to " + ((e == null) ? "NONE" : e.getName()));
			
			// Save the Flight Report
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.write(fr, ctx.getDB());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Return to the Flight Report
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", null, ctx.getID());
		result.setSuccess(true);
	}
}