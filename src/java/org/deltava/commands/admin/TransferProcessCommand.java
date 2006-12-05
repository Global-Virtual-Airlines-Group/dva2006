// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to display a Transfer Request for processing.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferProcessCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw notFoundException("Invalid Transfer Request - " + ctx.getID());

			// See if there's a checkride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(txreq.getCheckRideID());
			ctx.setAttribute("checkRide", cr, REQUEST);

			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();

			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(txreq.getID());
			ctx.setAttribute("pilot", usr, REQUEST);

			// Init the testing history
			TestingHistoryHelper testHistory = initTestHistory(usr, con);

			// Get the requested equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType newEQ = eqdao.get(txreq.getEquipmentType());
			EquipmentType currEQ = eqdao.get(usr.getEquipmentType());
			ctx.setAttribute("currentEQ", currEQ, REQUEST);
			ctx.setAttribute("eqType", newEQ, REQUEST);
			if (txreq.getRatingOnly()) {
				Collection<EquipmentType> eqTypes = new HashSet<EquipmentType>();
				eqTypes.add(currEQ);
				ctx.setAttribute("activeEQ", eqTypes, REQUEST);
			} else {
				ctx.setAttribute("activeEQ", eqdao.getActive(), REQUEST);
			}

			// Check if the user has passed the Captain's examination
			boolean hasCaptExam = testHistory.hasPassed(newEQ.getExamName(Ranks.RANK_C));

			// Check how many legs the user has completed
			GetFlightReportRecognition prdao = new GetFlightReportRecognition(con);
			int promoLegs = prdao.getPromotionCount(txreq.getID(), txreq.getEquipmentType());
			boolean hasLegs = (promoLegs >= newEQ.getPromotionLegs(Ranks.RANK_C));

			// Check if the user is eligible for promotion
			ctx.setAttribute("captExam", Boolean.valueOf(hasCaptExam), REQUEST);
			ctx.setAttribute("promoLegs", new Integer(promoLegs), REQUEST);
			ctx.setAttribute("captOK", Boolean.valueOf(hasCaptExam && hasLegs), REQUEST);

			// Determine new equipment ratings if approved
			Set<String> newRatings = new TreeSet<String>(usr.getRatings());
			newRatings.addAll(newEQ.getPrimaryRatings());
			newRatings.addAll(newEQ.getSecondaryRatings());
			ctx.setAttribute("newRatings", newRatings, REQUEST);
			
			// Get all aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);

			// Save the transfer request and access controller
			ctx.setAttribute("txReq", txreq, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/txRequestProcess.jsp");
		result.setSuccess(true);
	}
}