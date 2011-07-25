// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.Runway;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.GeoUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate the runways used.
 * @author Luke
 * @version 4.0
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
			
			// Get the DAO
			boolean isUpdated = false;
			GetNavAirway navdao = new GetNavAirway(con);
			
			// Load the departure runway
			Runway rD = null;
			if (afr.getTakeoffHeading() > -1) {
				Runway r = navdao.getBestRunway(afr.getAirportD(), afr.getFSVersion(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getTakeoffLocation());
					rD = new RunwayDistance(r, dist);
					isUpdated = !rD.equals(info.getRunwayD());
				}
			}

			// Load the arrival runway
			Runway rA = null;
			if (afr.getLandingHeading() > -1) {
				Runway r = navdao.getBestRunway(afr.getAirportA(), afr.getFSVersion(), afr.getLandingLocation(), afr.getLandingHeading());
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getLandingLocation());
					rA = new RunwayDistance(r, dist);
					isUpdated |= !rA.equals(info.getRunwayA());
				}
			}
			
			// Save the runways
			if (isUpdated) {
				SetACARSData awdao = new SetACARSData(con);
				awdao.writeRunways(info.getID(), rD, rA);
			}
			
			// Save the PIREP ID
			pirepID = afr.getID();
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("pirep", null, pirepID);
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}