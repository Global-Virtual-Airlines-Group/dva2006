// Copyright 2010, 2011, 2012, 2016, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FDRFlightReport;
import org.deltava.beans.navdata.Runway;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to manually update the departure and arrival runways for an ACARS flight.
 * @author Luke
 * @version 10.0
 * @since 3.0
 */

public class UpdateRunwayCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int pirepID = 0;
		try {
			Connection con = ctx.getConnection();
			
			// Get the PIREP
			GetFlightReportACARS prdao = new GetFlightReportACARS(con);
			FDRFlightReport afr = prdao.getACARS(ctx.getDB(), ctx.getID());
			if (afr == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, afr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot modify runways");
			
			// Load the flight data
			GetACARSData acdao = new GetACARSData(con);
			FlightInfo info = acdao.getInfo(ctx.getID());
			if (info == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());
			
			// Get the runway codes
			List<String> dRwy = StringUtils.split(ctx.getParameter("newRwyD"), " ");
			List<String> aRwy = StringUtils.split(ctx.getParameter("newRwyA"), " ");
			
			// Get the runways
			GetNavData navdao = new GetNavData(con);
			Runway rD = (dRwy == null) ? null : navdao.getRunway(afr.getAirportD(), dRwy.get(1), afr.getSimulator());
			Runway rA = (aRwy == null) ? null : navdao.getRunway(afr.getAirportA(), aRwy.get(1), afr.getSimulator());
			
			// Check if we've changed anything
			boolean isUpdated = false;
			if ((rD != null) && (!rD.equals(info.getRunwayD()))) {
				int dist = rD.distanceFeet(afr.getTakeoffLocation());
				info.setRunwayD(new RunwayDistance(rD, dist));
				isUpdated = true;
			}
			if ((rA != null) && (!rA.equals(info.getRunwayA()))) {
				int dist = rA.distanceFeet(afr.getLandingLocation());
				info.setRunwayA(new RunwayDistance(rA, dist));
				isUpdated = true;
			}
					
			// Save the runways
			if (isUpdated) {
				SetACARSRunway awdao = new SetACARSRunway(con);
				awdao.writeRunways(info.getID(), info.getRunwayD(), info.getRunwayA());
			}
			
			// Save the PIREP ID
			pirepID = afr.getID();
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the PIREP again
		CommandResult result = ctx.getResult();
		result.setURL("pirep", null, pirepID);
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}