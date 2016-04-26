// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.Gate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to recalculate the gates used.
 * @author Luke
 * @version 7.0
 * @since 5.1
 */

public class GateCalculateCommand extends AbstractCommand {

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

			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot modify gates");

			// Convert the flight report
			FDRFlightReport afr = (FDRFlightReport) fr;

			// Load the flight data
			GetACARSPositions acdao = new GetACARSPositions(con);
			FlightInfo info = acdao.getInfo(afr.getDatabaseID(DatabaseID.ACARS));
			if (info == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());

			// Load the positions
			List<? extends RouteEntry> entries = acdao.getRouteEntries(info.getID(), info.getArchived());
			GeoComparator dgc = new GeoComparator(entries.get(0), true);
			GeoComparator agc = new GeoComparator(entries.get(entries.size() - 1), true);

			// Get the closest departure gate
			GetGates gdao = new GetGates(con);
			SortedSet<Gate> dGates = new TreeSet<Gate>(dgc);
			dGates.addAll(gdao.getAllGates(afr.getAirportD(), info.getFSVersion()));
			Gate gD = dGates.isEmpty() ? null : dGates.first();
			boolean isUpdated = (gD != null) && !gD.equals(info.getGateD());
			
			// Get the closest arrival gate
			SortedSet<Gate> aGates = new TreeSet<Gate>(agc);
			aGates.addAll(gdao.getAllGates(afr.getAirportA(), info.getFSVersion()));
			Gate gA = aGates.isEmpty() ? null : aGates.first();
			isUpdated |= ((gA != null) && !gA.equals(info.getGateA()));
			
			// Save the gates
			if (isUpdated) {
				SetACARSRunway awdao = new SetACARSRunway(con);
				awdao.writeGates(info.getID(), gD, gA);
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