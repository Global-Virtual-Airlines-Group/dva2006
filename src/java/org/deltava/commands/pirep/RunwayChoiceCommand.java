// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.LandingRunways;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display runway calculation data.
 * @author Luke
 * @version 7.0
 * @since 4.2
 */

public class RunwayChoiceCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the PIREP
			GetFlightReportACARS prdao = new GetFlightReportACARS(con);
			FlightReport fr = prdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report ID - " + ctx.getID());
			else if (!fr.hasAttribute(FlightReport.ATTR_ACARS))
				throw notFoundException("Non-ACARS Flight Report - " + ctx.getID());
			
			// Convert the flight report
			ACARSFlightReport afr = (ACARSFlightReport) fr;
			ctx.setAttribute("pirep", afr, REQUEST);
			
			// Process the runways
			GetNavData navdao = new GetNavData(con);
			if (afr.getTakeoffHeading() > -1) {
				LandingRunways lrD = navdao.getBestRunway(afr.getAirportD(), afr.getSimulator(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
				ctx.setAttribute("rwysD", lrD, REQUEST);
			}
			
			if (afr.getLandingHeading() > -1) {
				LandingRunways lrA = navdao.getBestRunway(afr.getAirportA(), afr.getSimulator(), afr.getLandingLocation(), afr.getLandingHeading());
				ctx.setAttribute("rwysA", lrA, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/runwayChoices.jsp");
		result.setSuccess(true);
	}
}