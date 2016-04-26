// Copyright 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
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

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to pre-Approve a return flight charter when no schulede entry exists. 
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

public class ReturnCharterCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/returnCharter.jsp");
		
		boolean isRequest = (ctx.getParameter("eqType") != null);
		try {
			Connection con = ctx.getConnection();
			
			// Get the user
			int id = ctx.getID();
			GetPilot pdao = new GetPilot(con);
			Pilot p = (id == 0) ? ctx.getUser() : pdao.get(id);
			ctx.setAttribute("assignPilot", p, REQUEST);
			
			// Get the user's last flight
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.setQueryMax(10);
			List<FlightReport> results = frdao.getByPilot(p.getID(), new ScheduleSearchCriteria("SUBMITTED DESC"));
			FlightReport lf = null;
			for (FlightReport fr : results) {
				if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED)) {
					lf = fr;
					ctx.setAttribute("lastFlight", fr, REQUEST);
					break;
				}
			}
			
			// Validate that we need a return charter
			boolean hasFlight = true;
			if (lf != null) {
				GetSchedule sdao = new GetSchedule(con);
				int outFlightCount = sdao.getFlights(lf.getAirportA()).size();
				hasFlight = (outFlightCount > 0);
				ctx.setAttribute("hasFlight", Boolean.valueOf(hasFlight), REQUEST);
			}
			
			// If no last flight or schedule entry, return
			if (hasFlight || (lf == null)) {
				ctx.release();
				result.setURL("/jsp/assign/returnCharterInvalid.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Add aircraft/airline choices and return
			GetAircraft acdao = new GetAircraft(con);
			if (!isRequest) {
				Collection<ComboAlias> airlines = new LinkedHashSet<ComboAlias>();
				airlines.add(lf.getAirline());
				airlines.add(SystemData.getAirline(SystemData.get("airline.code")));
				ctx.setAttribute("airlines", airlines, REQUEST);
				
				Collection<Aircraft> acTypes = acdao.getAircraftTypes();
				for (Iterator<Aircraft> i = acTypes.iterator(); i.hasNext(); ) {
					Aircraft ac = i.next();
					if (ac.getRange() < lf.getDistance())
						i.remove();
					else if (!p.hasRating(ac.getName()))
						i.remove();
				}
				
				ctx.setAttribute("eqTypes", acTypes, REQUEST);
				ctx.release();
				
				result.setSuccess(true);
				return;
			}

			// Validate the aircraft
			Aircraft ac = acdao.get(ctx.getParameter("eqType"));
			if ((ac == null) || (ac.getRange() < lf.getDistance())) {
				ctx.setAttribute("eqType", ac, REQUEST);
				ctx.setAttribute("rangeWarning", Boolean.TRUE, REQUEST);
				ctx.release();
				
				result.setSuccess(true);
				return;
			}
			
			// Build the assignment information
			AssignmentInfo info = new AssignmentInfo(ac.getName());
			info.setAssignDate(Instant.now());
			info.setStatus(AssignmentInfo.RESERVED);
			info.setPurgeable(true);
			info.setRandom(true);
			
			// Get the airline
			Airline a = SystemData.getAirline(ctx.getParameter("airline"));
			if (a == null)
				a = SystemData.getAirline(SystemData.get("airline.code"));
			
			// Build the leg
			AssignmentLeg leg = new AssignmentLeg(a, lf.getFlightNumber(), Math.min(8, lf.getLeg() + 1));
			leg.setEquipmentType(info.getEquipmentType());
			leg.setAirportD(lf.getAirportA());
			leg.setAirportA(lf.getAirportD());
			info.addAssignment(leg);
			info.setPilotID(ctx.getID());
			
			// Build the PIREP
			FlightReport fr = new FlightReport(leg);
			fr.setDatabaseID(DatabaseID.PILOT, p.getID());
			fr.setDatabaseID(DatabaseID.DISPOSAL, ctx.getUser().getID());
			fr.setRank(p.getRank());
			fr.setDate(info.getAssignDate());
			fr.setAttribute(FlightReport.ATTR_CHARTER, true);
			fr.setComments("Return charter created by " + ctx.getUser().getName());
			
			// Start the transaction
			ctx.startTX();

			// Create the Flight Assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, SystemData.get("airline.db"));
			awdao.assign(info, info.getPilotID(), SystemData.get("airline.db"));
			
			// Write the Flight leg
			fr.setDatabaseID(DatabaseID.ASSIGN, info.getID());
			info.addFlight(fr);
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			
			// Commit and save pilot
			ctx.commitTX();
			ctx.setAttribute("assign", info, REQUEST);
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
		result.setSuccess(true);
	}
}