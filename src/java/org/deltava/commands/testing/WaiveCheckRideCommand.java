// Copyright 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to waive a Check Ride.
 * @author Luke
 * @version 8.0
 * @since 5.3
 */

public class WaiveCheckRideCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get command result
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();

			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Initialize the testing history helper
			TestingHistoryHelper testHistory = initTestHistory(p, con);

			// Get the active Equipment Profiles and determine what we are missing check rides for
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Map<String, EquipmentType> activeEQ = CollectionUtils.createMap(eqdao.getAvailable(SystemData.get("airline.code")), EquipmentType::getName);
			for (Iterator<EquipmentType> i = activeEQ.values().iterator(); i.hasNext();) {
				EquipmentType eq = i.next();
				if (testHistory.hasCheckRide(eq))
					i.remove();
			}
			
			// Save status attributes
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("eqTypes", activeEQ.values(), REQUEST);

			// If we're just doing a GET, then redirect to the JSP
			String eqType = ctx.getParameter("eqType");
			if (eqType == null) {
				ctx.release();
				ctx.setAttribute("availableEQ", activeEQ.values(), REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/testing/waiveCheckRide.jsp");
				result.setSuccess(true);
				return;
			}

			// Validate the equipment program
			EquipmentType eq = activeEQ.get(eqType);
			if (eq == null)
				throw securityException("Cannot waive check ride for " + eqType);

			// Create the check ride record
			CheckRide cr = new CheckRide(eqType + " Check Ride Waiver");
			cr.setAuthorID(p.getID());
			cr.setType(RideType.WAIVER);
			cr.setDate(Instant.now());
			cr.setSubmittedOn(cr.getDate());
			cr.setEquipmentType(eq);
			cr.setAircraftType(eq.getName());
			cr.setScore(true);
			cr.setStatus(TestStatus.SCORED);
			cr.setScorerID(ctx.getUser().getID());
			cr.setScoredOn(cr.getDate());
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setComments(ctx.getParameter("comments"));
			if (p.getProficiencyCheckRides())
				cr.setExpirationDate(Instant.now().plus(SystemData.getInt("testing.currency.validity", 365), ChronoUnit.DAYS));

			// Save the check ride
			SetExam wdao = new SetExam(con);
			wdao.write(cr);
			
			// Set status attributes
			ctx.setAttribute("eqType", eq, REQUEST);
			ctx.setAttribute("cr", cr, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isWaiver", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/examUpdate.jsp");
		result.setSuccess(true);
	}
}