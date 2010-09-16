// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.List;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.ACARSFlightReport;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate takeoff and touchdown points. 
 * @author Luke
 * @version 3.3
 * @since 3.1
 */

public class UpdateTouchdownCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(UpdateTouchdownCommand.class);

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
			
			// Load the ACARS data
			GetACARSData fddao = new GetACARSData(con);
			FlightInfo info = fddao.getInfo(ctx.getID());
			if (info == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());
			
			// Load the Flight Report
			GetFlightReportACARS frdao = new GetFlightReportACARS(con);
			ACARSFlightReport afr = frdao.getACARS(SystemData.get("airline.db"), info.getID());
			if (afr == null)
				throw notFoundException("Invalid ACARS Flight ID - " + info.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, afr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot modify takeoff/touchdown points");
			
			// Load the takeoff/touchdown data
			pirepID = afr.getID();
			List<RouteEntry> tdEntries = fddao.getTakeoffLanding(info.getID(), info.getArchived());
			if (tdEntries.size() > 2) {
				int ofs = 0;
				RouteEntry entry = tdEntries.get(0);
				GeoPosition adPos = new GeoPosition(info.getAirportD());
				while ((ofs < (tdEntries.size() - 1)) && (adPos.distanceTo(entry) < 15) && (entry.getVerticalSpeed() > 0)) {
					ofs++;
					entry = tdEntries.get(ofs);
				}

				// Trim out spurious takeoff entries
				if (ofs > 0)
					tdEntries.subList(0, ofs - 1).clear();
				if (tdEntries.size() > 2)
					tdEntries.subList(1, tdEntries.size() - 1).clear();
				
				// Save the entry points
				if (tdEntries.size() > 0) {
					afr.setTakeoffLocation(tdEntries.get(0));
					afr.setTakeoffHeading(tdEntries.get(0).getHeading());
					if (tdEntries.size() > 1) {
						afr.setLandingLocation(tdEntries.get(1));
						afr.setLandingHeading(tdEntries.get(1).getHeading());
					}

					// Save the flight report
					SetFlightReport frwdao = new SetFlightReport(con);
					frwdao.writeACARS(afr, SystemData.get("airline.db"));
				}
			} else
				log.warn("Cannot updated takeoff/touchdown - " + tdEntries.size() + " touchdown points");
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Redirect back to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", null, pirepID);
		result.setSuccess(true);
	}
}