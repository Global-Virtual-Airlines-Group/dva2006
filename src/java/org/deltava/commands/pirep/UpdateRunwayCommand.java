// Copyright 2010, 2011, 2012, 2016, 2019, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.Runway;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to manually update the departure and arrival runways for an ACARS flight.
 * @author Luke
 * @version 10.3
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
				throw notFoundException(String.format("Invalid ACARS Flight ID - %d", Integer.valueOf(ctx.getID())));
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, afr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException(String.format("Cannot modify Runways for ACARS Flight %d", Integer.valueOf(ctx.getID())));
			
			// Load the flight data
			GetACARSData acdao = new GetACARSData(con);
			FlightInfo info = acdao.getInfo(ctx.getID());
			if (info == null)
				throw notFoundException(String.format("Invalid ACARS Flight ID - %d", Integer.valueOf(ctx.getID())));
			
			// Get the runway codes
			List<String> dRwy = StringUtils.split(ctx.getParameter("newRwyD"), " ");
			List<String> aRwy = StringUtils.split(ctx.getParameter("newRwyA"), " ");
			
			// Get the runways
			GetNavData navdao = new GetNavData(con);
			Runway rD = (dRwy == null) ? null : navdao.getRunway(afr.getAirportD(), dRwy.get(1), afr.getSimulator());
			Runway rA = (aRwy == null) ? null : navdao.getRunway(afr.getAirportA(), aRwy.get(1), afr.getSimulator());
			
			// Check if we've changed anything
			boolean isUpdated = false;
			if (rD != null) {
				int dist = rD.distanceFeet(afr.getTakeoffLocation());
				double delta = GeoUtils.delta(rD.getHeading(), GeoUtils.course(rD, afr.getTakeoffLocation()));
				if (delta > 90)
					dist = -dist;
				
				boolean runwayChanged = !rD.equals(info.getRunwayD());
				int oldDistance = (info.getRunwayD() != null) ? ((RunwayDistance)info.getRunwayD()).getDistance() : 0;
				if (runwayChanged || (dist != oldDistance)) {
					isUpdated = true;
					info.setRunwayD(new RunwayDistance(rD, dist));
					if (runwayChanged)
						afr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, String.format("Updated departure Runway to %s", info.getRunwayD().getName()));
					else if (Math.abs(dist - oldDistance) > 200)
						afr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, String.format("Updated takeoff distance from %d to %d feet", Integer.valueOf(oldDistance), Integer.valueOf(dist)));
				}
			}
			if (rA != null) {
				int dist = rA.distanceFeet(afr.getLandingLocation());
				double delta = GeoUtils.delta(rA.getHeading(), GeoUtils.course(rA, afr.getLandingLocation()));
				if (delta > 90)
					dist = -dist;
				
				boolean runwayChanged = !rA.equals(info.getRunwayA());
				int oldDistance = (info.getRunwayA() != null) ? ((RunwayDistance)info.getRunwayA()).getDistance() : 0;
				if (runwayChanged || (dist != oldDistance)) {
					isUpdated = true;
					info.setRunwayA(new RunwayDistance(rA, dist));
					if (runwayChanged)
						afr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, String.format("Updated arrival Runway to %s", info.getRunwayA().getName()));
					else if (Math.abs(dist - oldDistance) > 200)
						afr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, String.format("Updated touchdown distance from %d to %d feet", Integer.valueOf(oldDistance), Integer.valueOf(dist)));
				}
			}
					
			// Save the runways and status history
			if (isUpdated) {
				ctx.startTX();
				SetACARSRunway awdao = new SetACARSRunway(con);
				awdao.writeRunways(info.getID(), info.getRunwayD(), info.getRunwayA());
				
				SetAggregateStatistics stdao = new SetAggregateStatistics(con);
				stdao.updateLanding(afr);
				
				SetFlightReport frwdao = new SetFlightReport(con);
				frwdao.writeHistory(afr.getStatusUpdates(), ctx.getDB());
				ctx.commitTX();
			}
			
			pirepID = afr.getID();
		} catch (DAOException de) {
			ctx.rollbackTX();
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