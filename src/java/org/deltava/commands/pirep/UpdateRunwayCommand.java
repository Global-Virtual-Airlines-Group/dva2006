// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.ACARSFlightReport;
import org.deltava.beans.navdata.Runway;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually update the departure and arrival runways for an ACARS flight.
 * @author Luke
 * @version 3.0
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
			ACARSFlightReport afr = prdao.getACARS(SystemData.get("airline.db"), ctx.getID());
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
			Runway rD = navdao.getRunway(afr.getAirportD().getICAO(), dRwy.get(1));
			Runway rA = navdao.getRunway(afr.getAirportA().getICAO(), aRwy.get(1));
			
			// Check if we've changed anything
			boolean isUpdated = false;
			if ((rD != null) && (!rD.equals(info.getRunwayD()))) {
				int dist = GeoUtils.distanceFeet(rD, afr.getTakeoffLocation());
				info.setRunwayD(new RunwayDistance(rD, dist));
				isUpdated = true;
			}
			if ((rA != null) && (!rA.equals(info.getRunwayA()))) {
				int dist = GeoUtils.distanceFeet(rA, afr.getLandingLocation());
				info.setRunwayA(new RunwayDistance(rA, dist));
				isUpdated = true;
			}
					
			// Save the runways
			if (isUpdated) {
				SetACARSData awdao = new SetACARSData(con);
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