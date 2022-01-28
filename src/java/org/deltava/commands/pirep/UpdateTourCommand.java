// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.stats.Tour;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update Flght Tours linked to a Flight Report.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class UpdateTourCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the Flight Tour
			int tourID = StringUtils.parse(ctx.getParameter("flightTour"), 0);
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(tourID, ctx.getDB());
			if ((t == null) && (tourID != 0))
				throw notFoundException("Invalid Flight Tour - " + tourID);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanAdjustEvents())
				throw securityException("Cannot update Flight Report Tour");
			
			// Check leg eligibility
			if (t != null) {
				TourFlightHelper tfh = new TourFlightHelper(fr, true);
				tfh.addFlights(frdao.getByTour(fr.getAuthorID(), t.getID(), ctx.getDB()));
				int leg = tfh.isLeg(t);
				fr.setAttribute(FlightReport.ATTR_ROUTEWARN, fr.hasAttribute(FlightReport.ATTR_ROUTEWARN) && (leg == 0));
				if (leg == 0) {
					ctx.setAttribute("tour", t, REQUEST);
					ctx.setAttribute("pirep", fr, REQUEST);
					result.setURL("/jsp/pilot/invalidTour.jsp");
					result.setSuccess(true);
					return;
				}
			}
			
			// Update the flight report
			fr.setDatabaseID(DatabaseID.TOUR, tourID);
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Updated Flight Tour to " + ((t == null) ? "NONE" : t.getName()));
			
			// Save the flight report
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.write(fr, ctx.getDB());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", null, ctx.getID());
		result.setSuccess(true);
	}
}