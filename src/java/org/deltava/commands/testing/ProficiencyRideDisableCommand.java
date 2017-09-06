// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.deltava.beans.*;
import org.deltava.beans.testing.IneligibilityException;
import org.deltava.beans.testing.TestingHistoryHelper;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
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
			
			// Load the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Unknown Pilot - " + userID);
			else if (!p.getProficiencyCheckRides())
				throw new CommandException("Proficiency check rides already disabled");
			else if (!SystemData.getBoolean("testing.currency.enabled"))
				throw new CommandException("Proficiency check rides not enabled for Airline");
			
			// Load exams, without expiration dates 
			p.setProficiencyCheckRides(false);
			TestingHistoryHelper testHelper = initTestHistory(p, con);
			
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

			// Status update
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.CURRENCY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Disabled currency Check Rides");
			upds.add(upd);
			
			// Set status attributes
			Collection<String> ratingDelta = CollectionUtils.getDelta(newRatings, p.getRatings());
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("newRatings", newRatings, REQUEST);
			ctx.setAttribute("ratingDelta", ratingDelta, REQUEST);
			if (!ratingDelta.isEmpty()) {
				StatusUpdate upd2 = new StatusUpdate(p.getID(), StatusUpdate.RATING_ADD);
				upd2.setAuthorID(ctx.getUser().getID());
				upd2.setDescription("Ratings added: " + StringUtils.listConcat(ratingDelta, ", "));
				upds.add(upd2);
			}
			
			// Update the pilot
			ctx.startTX();
			SetPilot pwdao = new SetPilot(con);
			p.setProficiencyCheckRides(false);
			p.addRatings(newRatings);
			pwdao.write(p);
			
			// Write status updates
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upds);
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