// Copyright 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.TestingHistoryHelper;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

/**
 * A Web Site Command to promote a Pilot to Captain.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class PromoteCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Check our access
			PilotAccessControl access = new PilotAccessControl(ctx, usr);
			access.validate();
			if (!access.getCanPromote())
				throw securityException("Cannot promote " + usr.getName());
			
			// Init the test history
			TestingHistoryHelper testHistory = initTestHistory(usr, con);
			
			// Make sure we are a First Officer
			if (!Ranks.RANK_FO.equals(usr.getRank()))
				throw new CommandException(usr.getName() + " is not a First Officer", false);
			
			// Make sure we have passed the examination
			EquipmentType eq = testHistory.getEquipmentType();
			if (!testHistory.hasPassed(eq.getExamNames(Ranks.RANK_C)))
				throw new CommandException(usr.getName() + " has not passed Captain's exams", false);
			
			// Make sure we have the legs
			if (testHistory.getFlightLegs(eq) < eq.getPromotionLegs())
				throw new CommandException(usr.getName() + " has insufficient flight legs", false);
			
			// Determine if we can jump to SC
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			boolean isSC = sudao.isSeniorCaptain(usr.getID());
			usr.setRank(isSC ? Ranks.RANK_SC : Ranks.RANK_C);
			
			// Create the status update bean
			StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.INTPROMOTION);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Promoted to " + usr.getRank() + ", " + usr.getEquipmentType());
			
			// Start a transaction
			ctx.startTX();
			
			// Write the Pilot Profile
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(usr);
			
			// Write the Status Update
			SetStatusUpdate wdao = new SetStatusUpdate(con);
			wdao.write(upd);
			
			// Invalidate the caches
			GetPilotRecognition.invalidate(usr.getEquipmentType());
			
			// Commit and save the pilot in the request 
			ctx.commitTX();
			ctx.setAttribute("pilot", usr, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}
}