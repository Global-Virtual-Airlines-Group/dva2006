// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to disable Proficiency Check Rides for a Pilot.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class ProficiencyRideDisableCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		if (ctx.isUserInRole("HR") && (ctx.getID() != 0))
			userID = ctx.getID();

		try {
			Connection con = ctx.getConnection();
			
			// Load the Pilot and the exams
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Unknown Pilot - " + userID);
			else if (!p.getProficiencyCheckRides())
				throw new CommandException("Proficiency check rides already disabled");
			else if (!SystemData.getBoolean("testing.currency.enabled"))
				throw new CommandException("Proficiency check rides not enabled for Airline");

			// Determine checkrides that need expiration dates removed
			TestingHistoryHelper testHelper = initTestHistory(p, con);
			Collection<CheckRide> updatedRides = testHelper.getExams().stream().filter(CheckRide.class::isInstance).map(CheckRide.class::cast).filter(cr -> (cr.getType() != RideType.CURRENCY)).collect(Collectors.toList());
			updatedRides.forEach(cr -> cr.setExpirationDate(null));

			// Status update
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.CURRENCY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Disabled currency Check Rides");
			
			// Write the updated exams
			ctx.startTX();
			SetExam ewdao = new SetExam(con);
			for (CheckRide cr : updatedRides)
				ewdao.write(cr);
			
			// Update the pilot
			SetPilot pwdao = new SetPilot(con);
			p.setProficiencyCheckRides(false);
			pwdao.write(p);
			
			// Write status update
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upd);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/proficiencyRideDisabled.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}