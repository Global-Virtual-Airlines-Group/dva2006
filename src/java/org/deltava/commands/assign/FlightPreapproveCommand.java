// Copyright 2008, 2009, 2010, 2011, 2012, 2013, 2016, 2017, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to allow staff members to pre-approve non-standard flight routes.
 * @author Luke
 * @version 10.5
 * @since 2.1
 */

public class FlightPreapproveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get command result
		CommandResult result = ctx.getResult();

		// Check our access
		PIREPAccessControl ac = new PIREPAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanPreApprove())
			throw securityException("Cannot Pre-Approve Flight");

		// Check for a GET and redirect
		if (ctx.getParameter("airportD") == null) {
			ctx.setAttribute("pilotID", Integer.valueOf(ctx.getID()), REQUEST);

			// Build Airline list
			Collection<ComboAlias> airlines = new ArrayList<ComboAlias>();
			airlines.add(ComboUtils.fromString("All Airlines", "all"));
			SystemData.getAirlines().stream().filter(Airline::getActive).forEach(airlines::add);			
			ctx.setAttribute("airlines", airlines, REQUEST);

			try {
				Connection con = ctx.getConnection();

				// Validate that the pilot exists
				GetPilot pdao = new GetPilot(con);
				Pilot usr = pdao.get(ctx.getID());
				if (usr == null)
					throw notFoundException("Unknown Pilot ID - " + ctx.getID());

				// Save pilot in request
				ctx.setAttribute("assignPilot", usr, REQUEST);

				// Get the equipment types
				GetAircraft adao = new GetAircraft(con);
				ctx.setAttribute("eqTypes", adao.getAircraftTypes(), REQUEST);

				// Get the number of charter flights
				int interval = SystemData.getInt("schedule.charter.count_days", 90);
				GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
				ctx.setAttribute("charterFlights", Integer.valueOf(stdao.getCharterCount(usr.getID(), 0, null)), REQUEST);
				ctx.setAttribute("charterFlightsInterval", Integer.valueOf(stdao.getCharterCount(usr.getID(), interval, Instant.now())), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}

			result.setURL("/jsp/assign/preApprove.jsp");
			result.setSuccess(true);
			return;
		}

		// Build the assignment information
		AssignmentInfo info = new AssignmentInfo(ctx.getParameter("eqType"));
		info.setAssignDate(Instant.now());
		info.setStatus(AssignmentStatus.RESERVED);
		info.setPurgeable(true);
		info.setRandom(true);

		// Get the airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		if (aD == null)
			aD = SystemData.getAirport(ctx.getParameter("airportDCode"));
		if (aA == null)
			aA = SystemData.getAirport(ctx.getParameter("airportACode"));

		// Get the airline
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		if (a == null)
			a = SystemData.getAirline(SystemData.get("airline.code"));

		// Build the leg
		AssignmentLeg leg = new AssignmentLeg(a, StringUtils.parse(ctx.getParameter("flight"), 1), StringUtils.parse(ctx.getParameter("leg"), 1));
		leg.setEquipmentType(info.getEquipmentType());
		leg.setAirportD(aD);
		leg.setAirportA(aA);
		info.addAssignment(leg);
		info.setPilotID(ctx.getID());
		
		// Build the Charter Request
		CharterRequest creq = new CharterRequest();
		creq.setAuthorID(ctx.getUser().getID());
		creq.setCreatedOn(Instant.now());
		creq.setAirportD(aD);
		creq.setAirportA(aA);
		creq.setAirline(a);
		creq.setEquipmentType(leg.getEquipmentType());
		creq.setDisposalID(ctx.getUser().getID());
		creq.setDisposedOn(creq.getCreatedOn().plusSeconds(1));
		creq.setStatus(CharterRequest.RequestStatus.APPROVED);
		creq.setComments("Pre-Approved Flight");

		try {
			Connection con = ctx.getConnection();

			// Validate that the pilot exists
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Unknown Pilot ID - " + ctx.getID());

			// Build the PIREP
			FlightReport fr = new FlightReport(leg);
			fr.setDatabaseID(DatabaseID.PILOT, usr.getID());
			fr.setDatabaseID(DatabaseID.DISPOSAL, ctx.getUser().getID());
			fr.setRank(usr.getRank());
			fr.setDate(info.getAssignDate());
			fr.setAttribute(FlightReport.ATTR_CHARTER, true);

			// Start the transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, ctx.getDB());
			awdao.assign(info, info.getPilotID(), ctx.getDB());
			awdao.write(creq);

			// Write the Flight leg
			fr.setDatabaseID(DatabaseID.ASSIGN, info.getID());
			info.addFlight(fr);
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Pre-Approved Flight - Request %d", Integer.valueOf(creq.getID())));
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			ctx.commitTX();

			// Save pilot in request
			ctx.setAttribute("assignPilot", usr, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isPreApprove", Boolean.TRUE, REQUEST);
		ctx.setAttribute("assign", info, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}