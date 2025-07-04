// Copyright 2010, 2011, 2012, 2015, 2016, 2018, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.Compression;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FDRFlightReport;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to recalculate takeoff and touchdown points. 
 * @author Luke
 * @version 11.1
 * @since 3.1
 */

public class UpdateTouchdownCommand extends AbstractCommand {
	
	private static final Logger log = LogManager.getLogger(UpdateTouchdownCommand.class);

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
			FDRFlightReport afr = frdao.getACARS(ctx.getDB(), info.getID());
			if (afr == null)
				throw notFoundException("Invalid ACARS Flight ID - " + info.getID());
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, afr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot modify takeoff/touchdown points");
			
			// Load the takeoff/touchdown data
			pirepID = afr.getID(); List<? extends RouteEntry> tdEntries = null;
			if (!info.getArchived())
				tdEntries = fddao.getTakeoffLanding(info.getID());
			else {
				try {
					File f = ArchiveHelper.getPositions(info.getID());
					Compression c = Compression.detect(f);
					try (InputStream in = c.getStream(new BufferedInputStream(new FileInputStream(f)))) {
						GetSerializedPosition psdao = new GetSerializedPosition(in);
						tdEntries = psdao.read().stream().filter(re -> re.isFlagSet(ACARSFlags.TOUCHDOWN)).collect(Collectors.toList());
					}
				} catch (IOException ie) {
					throw new DAOException(ie);
				}
			}
			
			if (tdEntries.size() > 2) {
				int ofs = 0;
				ACARSRouteEntry entry = (ACARSRouteEntry) tdEntries.get(0);
				GeoPosition adPos = new GeoPosition(info.getAirportD());
				while ((ofs < (tdEntries.size() - 1)) && (adPos.distanceTo(entry) < 15) && (entry.getVerticalSpeed() > 0)) {
					ofs++;
					entry = (ACARSRouteEntry) tdEntries.get(ofs);
				}

				// Trim out spurious takeoff entries
				if (ofs > 0)
					tdEntries.subList(0, ofs - 1).clear();
				if (tdEntries.size() > 2)
					tdEntries.subList(1, tdEntries.size() - 1).clear();
			}
				
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
				frwdao.writeACARS(afr, ctx.getDB());
			} else
				log.warn("Cannot update takeoff/touchdown - {} touchdown points", Integer.valueOf(tdEntries.size()));
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