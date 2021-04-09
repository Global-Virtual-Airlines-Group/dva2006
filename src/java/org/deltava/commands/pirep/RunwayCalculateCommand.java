// Copyright 2011, 2012, 2016, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to recalculate the runways used.
 * @author Luke
 * @version 10.0
 * @since 4.0
 */

public class RunwayCalculateCommand extends AbstractCommand {

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
			FlightReport fr = prdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report ID - " + ctx.getID());
			else if (!fr.hasAttribute(FlightReport.ATTR_ACARS))
				throw notFoundException("Non-ACARS Flight Report - " + ctx.getID());

			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot modify runways");
			
			// Convert the flight report
			FDRFlightReport afr = (FDRFlightReport) fr;

			// Load the flight data
			GetACARSData acdao = new GetACARSData(con);
			FlightInfo info = acdao.getInfo(afr.getDatabaseID(DatabaseID.ACARS));
			if (info == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());

			// Get the DAO
			boolean isUpdated = false;
			GetNavAirway navdao = new GetNavAirway(con);

			// Load the departure runway
			Runway rD = null;
			if (afr.getTakeoffHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(afr.getAirportD(), afr.getSimulator(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = r.distanceFeet(afr.getTakeoffLocation());
					rD = new RunwayDistance(r, dist);
					isUpdated = !rD.equals(info.getRunwayD());
				}
			}

			// Load the arrival runway
			Runway rA = null;
			if (afr.getLandingHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(afr.getAirportA(), afr.getSimulator(), afr.getLandingLocation(), afr.getLandingHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = r.distanceFeet(afr.getLandingLocation());
					rA = new RunwayDistance(r, dist);
					isUpdated |= !rA.equals(info.getRunwayA());
				}
			}

			// Save the runways
			if (isUpdated) {
				SetACARSRunway awdao = new SetACARSRunway(con);
				awdao.writeRunways(info.getID(), rD, rA);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("pirep", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}