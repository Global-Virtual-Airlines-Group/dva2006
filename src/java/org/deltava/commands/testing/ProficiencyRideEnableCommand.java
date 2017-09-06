// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to enable Proficiency Check Rides for a Pilot.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class ProficiencyRideEnableCommand extends AbstractTestHistoryCommand {

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

		boolean confirm = Boolean.valueOf((String) ctx.getCmdParameter(OPERATION, null)).booleanValue();
		try {
			Connection con = ctx.getConnection();
			
			// Load the Pilot and the exams
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Unknown Pilot - " + userID);
			else if (p.getProficiencyCheckRides())
				throw new CommandException("Proficiency check rides already enabled");
			else if (!SystemData.getBoolean("testing.currency.enabled"))
				throw new CommandException("Proficiency check rides not enabled for Airline");
			
			// Load exam history
			TestingHistoryHelper testHelper = initTestHistory(p, con);
			testHelper.applyExpiration(SystemData.getInt("testing.currency.validity", 365) + 30);
			
			// Go back and rebuild the list of things we are eligible for
			Collection<String> newRatings = new TreeSet<String>();
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> newEQ = eqdao.getActive();
			for (Iterator<EquipmentType> i = newEQ.iterator(); i.hasNext(); ) {
				EquipmentType eq = i.next();
				try {
					testHelper.canSwitchTo(eq);
					newRatings.addAll(eq.getRatings());
				} catch (IneligibilityException ie) {
					i.remove();
				}
			}
			
			// Status update collection
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.CURRENCY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Enabled currency Check Rides");
			upds.add(upd);
			
			// If we are not eligible for our program, create a 30-day waiver
			CheckRide wcr = null;
			if (!newEQ.contains(testHelper.getEquipmentType())) {
				wcr = new CheckRide("Initial Proficiency Waiver");
				wcr.setType(RideType.WAIVER);
				wcr.setDate(Instant.now());
				wcr.setPassFail(true);
				wcr.setAuthorID(ctx.getUser().getID());
				wcr.setScoredOn(Instant.now());
				wcr.setExpirationDate(Instant.now().plus(30, ChronoUnit.DAYS));
				wcr.setEquipmentType(testHelper.getEquipmentType());
				wcr.setComments("Proficiency Check Rides enabled, waiver for current program");
				testHelper.add(wcr);
				newEQ.add(testHelper.getEquipmentType());
			}

			// Set status attributes
			Collection<String> ratingDelta = CollectionUtils.getDelta(p.getRatings(), newRatings);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("waiver", wcr, REQUEST);
			ctx.setAttribute("newRatings", newRatings, REQUEST);
			ctx.setAttribute("ratingDelta", ratingDelta, REQUEST);
			if (!ratingDelta.isEmpty()) {
				StatusUpdate upd2 = new StatusUpdate(p.getID(), StatusUpdate.RATING_REMOVE);
				upd2.setAuthorID(ctx.getUser().getID());
				upd2.setDescription("Ratings removed: " + StringUtils.listConcat(ratingDelta, ", "));
				upds.add(upd2);
			}
			
			// If we're confirming, make the changes
			if (confirm) {
				ctx.setAttribute("doConfirm", Boolean.TRUE, REQUEST);
				ctx.startTX();
				
				// Write the waiver
				if (wcr != null) {
					SetExam ewdao = new SetExam(con);
					ewdao.write(wcr);
				}
				
				// Update the pilot profile and ratings
				p.setProficiencyCheckRides(true);
				p.removeRatings(ratingDelta);
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p);
				
				// Write status updates
				SetStatusUpdate swdao = new SetStatusUpdate(con);
				swdao.write(upds);
				
				// Commit
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/proficiencyRideEnabled.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}