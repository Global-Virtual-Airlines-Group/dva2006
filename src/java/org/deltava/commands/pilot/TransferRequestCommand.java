// Copyright 2005, 2006, 2007, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.TransferRequest;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to request a transfer to a different Equipment program.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class TransferRequestCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// Get the Pilot object
		Pilot p = (Pilot) ctx.getUser();
		ctx.setAttribute("pilot", p, REQUEST);
		
		// Determine if we are requesting an additional rating only
		boolean isRating = "rating".equals(ctx.getCmdParameter(OPERATION, null));
		ctx.setAttribute("isRating", Boolean.valueOf(isRating), REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Initialize the testing history helper
			TestingHistoryHelper testHistory = initTestHistory(p, con);

			// Get the active Equipment Profiles and determine what we can switch to
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Map<String, EquipmentType> activeEQ = CollectionUtils.createMap(eqdao.getAvailable(SystemData.get("airline.code")), "name");
			for (Iterator<EquipmentType> i = activeEQ.values().iterator(); i.hasNext(); ) {
				EquipmentType eq = i.next();
				try {
					boolean checkSwitch = true;
					if (!testHistory.hasCheckRide(eq)) {
						testHistory.canRequestCheckRide(eq);
						checkSwitch = false;
					}
					
					if (checkSwitch)
						testHistory.canSwitchTo(eq);
					
					if (isRating && !testHistory.canRequestRatings(eq))
						i.remove();
				} catch (IneligibilityException ie) {
					i.remove();
				}
			}

			// If we're just doing a GET, then redirect to the JSP
			String eqType = ctx.getParameter("eqType");
			if (eqType == null) {
				ctx.release();
				ctx.setAttribute("availableEQ", activeEQ.values(), REQUEST);
				ctx.setAttribute("isEmpty", Boolean.valueOf(activeEQ.isEmpty()), REQUEST);

				// Forward to the JSP
				result.setURL(activeEQ.isEmpty() ? "/jsp/admin/txRequestUpdate.jsp" : "/jsp/pilot/txRequestNew.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if we have a pending transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			if (txdao.hasTransfer(p.getID()))
				throw securityException("Pending Equipment Transfer request");

			// Check if we can transfer into that program
			if (!activeEQ.containsKey(eqType))
				throw securityException("Cannot request transfer to " + eqType);
			
			// Populate the transfer request
			EquipmentType eq = activeEQ.get(eqType);
			TransferRequest txreq = new TransferRequest(p.getID(), eqType);
			if (!eq.getOwner().equals(SystemData.get("airline.code")))
				txreq.setRatingOnly(true);
			else
				txreq.setRatingOnly(Boolean.valueOf(ctx.getParameter("ratingOnly")).booleanValue());
			
			// Set status to approved if we can transfter
			try {
				testHistory.canSwitchTo(eq);
				txreq.setStatus(TransferRequest.OK);
			} catch (IneligibilityException ie) {
				txreq.setStatus(TransferRequest.PENDING);
			}

			// Save the transfer request
			SetTransferRequest wdao = new SetTransferRequest(con);
			wdao.create(txreq, eq.getOwner().getDB());

			// Store the transfer request in the request
			ctx.setAttribute("txReq", txreq, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status for JSP
		ctx.setAttribute("isNew", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/admin/txRequestUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}