// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to allow staff members to pre-approve non-standard flight routes.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class FlightPreapproveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get command result
		CommandResult result = ctx.getResult();
		
		// Check for a GET and redirect
		if (ctx.getParameter("airportD") == null) {
			ctx.setAttribute("pilotID", new Integer(ctx.getID()), REQUEST);
			ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
			ctx.setAttribute("airports", CollectionUtils.sort(new HashSet<Airport>(SystemData.getAirports().values()), 
					new AirportComparator(AirportComparator.NAME)), REQUEST);
			
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
				GetAircraft adao = new GetAircraft (con);
				ctx.setAttribute("eqTypes", adao.getAircraftTypes(), REQUEST);
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
		info.setAssignDate(new Date());
		info.setStatus(AssignmentInfo.RESERVED);
		info.setPurgeable(true);
		info.setRandom(true);
		
		// Build the leg
		AssignmentLeg leg = new AssignmentLeg(SystemData.getAirline(ctx.getParameter("airline")),
				StringUtils.parse(ctx.getParameter("flight"), 1), 1);
		leg.setEquipmentType(info.getEquipmentType());
		leg.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		leg.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		info.addAssignment(leg);
		
		try {
			Connection con = ctx.getConnection();
			
			// Validate that the pilot exists
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Unknown Pilot ID - " + ctx.getID());
			
			// Build the PIREP
			info.setPilotID(usr);
			FlightReport fr = new FlightReport(leg);
			fr.setDatabaseID(FlightReport.DBID_PILOT, usr.getID());
			fr.setDatabaseID(FlightReport.DBID_DISPOSAL, ctx.getUser().getID());
			fr.setRank(usr.getRank());
			fr.setDate(info.getAssignDate());
			fr.setAttribute(FlightReport.ATTR_CHARTER, true);
			fr.setComments("Pre-Approved by " + ctx.getUser().getName());
			
			// Start the transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, SystemData.get("airline.db"));
            awdao.assign(info, info.getPilotID(), SystemData.get("airline.db"));
            
            // Write the Flight leg
            fr.setDatabaseID(FlightReport.DBID_ASSIGN, info.getID());
            SetFlightReport fwdao = new SetFlightReport(con);
            fwdao.write(fr);

			// Commit the transaction
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

		// Forward to the JSP
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}