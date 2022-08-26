// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to initiate a SimBrief route plot request.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class SimBriefCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check that we have a Navigraph account
		Pilot p = ctx.getUser();
		if (!p.hasID(ExternalID.NAVIGRAPH))
			throw securityException("No Navigraph ID");
		
		try {
			Connection con = ctx.getConnection();
			
			// Load the flight report
			if (ctx.getID() != 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				FlightReport fr = frdao.get(ctx.getID(), ctx.getDB());
				if (fr == null)
					throw notFoundException("Invalid Flight Report - " + ctx.getID());
				else if (fr.getStatus() != FlightStatus.DRAFT)
					throw securityException("Invalid Flight Report Status - " + fr.getStatus().getDescription());
			
				ctx.setAttribute("flight", fr, REQUEST);
			}
			
			// Get aircraft profiles
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(p.getID()), REQUEST);
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/simBrief.jsp");
		result.setSuccess(true);
	}
}