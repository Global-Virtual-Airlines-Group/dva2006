// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Fligt Report submissions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPSubmitCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the PIREP to submit
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport pirep = frdao.get(ctx.getID());
			if (pirep == null)
				throw notFoundException("Flight Report Not Found");

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, pirep);
			access.validate();
			if (!access.getCanSubmit())
				throw securityException("Cannot submit Flight Report #" + pirep.getID());

			// Get the Pilot profile of the individual who flew this flight
			Pilot pilot = (Pilot) ctx.getUser();
			if (pilot.getID() != pirep.getDatabaseID(FlightReport.DBID_PILOT)) {
				GetPilot pdao = new GetPilot(con);
				pilot = pdao.get(pirep.getDatabaseID(FlightReport.DBID_PILOT));
			}

			// Save the Pilot profile
			ctx.setAttribute("pilot", pilot, REQUEST);

			// Get our equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(ctx.getUser().getEquipmentType());
			ctx.setAttribute("eqType", eq, REQUEST);
			
			// Check if the pilot is rated in the equipment type
			if (!pilot.getRatings().contains(pirep.getEquipmentType()) && !eq.getRatings().contains(pirep.getEquipmentType())) {
				ctx.setAttribute("notRated", Boolean.TRUE, REQUEST);
				pirep.setAttribute(FlightReport.ATTR_NOTRATED, true);
			}

			// Check if this flight was flown with an equipment type in our primary ratings
			Collection<String> pTypeNames = eqdao.getPrimaryTypes(SystemData.get("airline.db"), pirep.getEquipmentType());
			if (pTypeNames.contains(pilot.getEquipmentType())) {
				for (Iterator<String> i = pTypeNames.iterator(); i.hasNext(); ) {
					String pName = i.next();
					EquipmentType peq = eqdao.get(pName);
					if ((peq == null) || (peq.getACARSPromotionLegs()))
						i.remove();
				}
				
				// Add programs if we still have any that do not require ACARS legs
				if (!pTypeNames.isEmpty()) {
					ctx.setAttribute("captEQ", Boolean.TRUE, REQUEST);
					ctx.setAttribute("promoteLegs", new Integer(eq.getPromotionLegs(Ranks.RANK_C)), REQUEST);
					pirep.setCaptEQType(pTypeNames);
				}
			}
			
			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(pirep);
			boolean isAcademy = ((sEntry != null) && sEntry.getAcademy());
			pirep.setAttribute(FlightReport.ATTR_ACADEMY, isAcademy);

			// Update the status of the PIREP
			pirep.setStatus(FlightReport.SUBMITTED);
			pirep.setSubmittedOn(new Date());

			// Check the schedule database and check the route pair
			int avgHours = sdao.getFlightTime(pirep.getAirportD().getIATA(), pirep.getAirportA().getIATA());
			if ((avgHours == 0) && (!isAcademy)) {
				pirep.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
				ctx.setAttribute("unknownRoute", Boolean.TRUE, REQUEST);
			} else {
				int minHours = (int) ((avgHours * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));

				if ((pirep.getLength() < minHours) || (pirep.getLength() > maxHours)) {
					pirep.setAttribute(FlightReport.ATTR_TIMEWARN, true);
					ctx.setAttribute("timeWarning", Boolean.TRUE, REQUEST);
					ctx.setAttribute("avgTime", new Double(avgHours), REQUEST);
				}
			}

			// Get the DAO and write the PIREP to the database
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(pirep);
			
			// Save the pirep in the request
			ctx.setAttribute("pirep", pirep, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status for JSP
		ctx.setAttribute("isSubmitted", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}