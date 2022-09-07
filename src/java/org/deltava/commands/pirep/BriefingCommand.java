// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.simbrief.BriefingPackage;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to display a SimBrief pilot briefing.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class BriefingCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			GetFlightReports frdao = new GetFlightReports(ctx.getConnection());
			FlightReport fr = frdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanViewSimBrief())
				throw securityException("Cannot view SimBrief data for Flight Report " + fr.getID());
			
			// Load the SimBrief package
			BriefingPackage pkg = frdao.getSimBrief(fr.getID(), ctx.getDB());
			if (pkg == null)
				throw notFoundException("No SimBrief data for Flight Report " + ctx.getID());
			
			// Save status attribute
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("pkg", pkg, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/simBriefText.jsp");
		result.setSuccess(true);
	}
}