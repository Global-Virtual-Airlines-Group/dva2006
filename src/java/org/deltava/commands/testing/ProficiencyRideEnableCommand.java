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
			int rideValidity = SystemData.getInt("testing.currency.validity", 365);
			TestingHistoryHelper testHelper = initTestHistory(p, con);
			Collection<EquipmentType> oldEQ = testHelper.getQualifiedPrograms();
			testHelper.applyExpiration(rideValidity + 30);
			
			// Go back and rebuild the list of things we are eligible for
			Collection<EquipmentType> newEQ = testHelper.getQualifiedPrograms();
			
			// Status update collection
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.CURRENCY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Enabled currency Check Rides");
			upds.add(upd);
			
			// If we are not eligible for our program, create a 30-day waiver
			List<CheckRide> waivers = new ArrayList<CheckRide>();
			Collection<EquipmentType> deltaEQ = CollectionUtils.getDelta(oldEQ, newEQ);
			if (!deltaEQ.isEmpty()) {
				Collection<String> waiverNames = new TreeSet<String>();
				Instant waiverDate = Instant.now().plus(30, ChronoUnit.DAYS);
				for (EquipmentType weq : deltaEQ) {
					CheckRide wcr = new CheckRide("Initial Proficiency Waiver");
					wcr.setType(RideType.WAIVER);
					wcr.setDate(Instant.now());
					wcr.setPassFail(true);
					wcr.setAuthorID(ctx.getUser().getID());
					wcr.setSubmittedOn(wcr.getDate());
					wcr.setScoredOn(wcr.getDate());
					wcr.setScorerID(ctx.getUser().getID());
					wcr.setExpirationDate(waiverDate);
					wcr.setEquipmentType(weq.getName());
					wcr.setOwner(SystemData.getApp(null));
					wcr.setComments("Proficiency Check Rides enabled, waiver for rated program");
					testHelper.add(wcr);
					waivers.add(wcr);
					newEQ.add(weq);
					waiverNames.add(weq.getName());
				}
				
				ctx.setAttribute("waiverExpiry", waiverDate, REQUEST);
				ctx.setAttribute("waiverNames", waiverNames, REQUEST);
			}

			// Set status attributes
			Collection<String> newRatings = testHelper.getQualifiedRatings();
			Collection<String> ratingDelta = CollectionUtils.getDelta(p.getRatings(), newRatings);
			ctx.setAttribute("pilot", p, REQUEST);
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
				ctx.setAttribute("upcomingExpirations", testHelper.getCheckRides(60), REQUEST);
				
				ctx.setAttribute("doConfirm", Boolean.TRUE, REQUEST);
				ctx.startTX();
				
				// Write the waiver(s)
				SetExam ewdao = new SetExam(con);
				for (CheckRide wcr : waivers)
					ewdao.write(wcr);
				
				// Update the pilot profile and ratings
				p.setProficiencyCheckRides(true);
				p.removeRatings(ratingDelta);
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p);
				
				// Write status updates
				SetStatusUpdate swdao = new SetStatusUpdate(con);
				swdao.write(upds);
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
		result.setType(confirm ? ResultType.REQREDIRECT : ResultType.FORWARD);
		result.setSuccess(true);
	}
}