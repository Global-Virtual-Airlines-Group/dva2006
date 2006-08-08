// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EquipmentType;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to request a transfer to a different Equipment program.
 * @author Luke
 * @version 1.0
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
			initTestHistory(p, con);

			// Get the active Equipment Profiles and determine what we can switch to
			GetEquipmentType eqdao = new GetEquipmentType(con);
			List<EquipmentType> activeEQ = new ArrayList<EquipmentType>(eqdao.getActive());
			for (Iterator i = activeEQ.iterator(); i.hasNext();) {
				EquipmentType eq = (EquipmentType) i.next();
				if (!_testHistory.canSwitchTo(eq) && !_testHistory.canRequestCheckRide(eq))
					i.remove();
				else if (isRating && !_testHistory.canRequestRatings(eq))
					i.remove();
			}

			// If we're just doing a GET, then redirect to the JSP
			String eqType = ctx.getParameter("eqType");
			if (eqType == null) {
				ctx.release();
				ctx.setAttribute("availableEQ", activeEQ, REQUEST);
				ctx.setAttribute("isEmpty", Boolean.valueOf(activeEQ.isEmpty()), REQUEST);

				// Forward to the JSP
				result.setURL(activeEQ.isEmpty() ? "/jsp/admin/txRequestUpdate.jsp" : "/jsp/pilot/txRequestNew.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if we have a pending transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getUser().getID());
			if (txreq != null)
				throw securityException("Pending Equipment Transfer request");

			// Check if we can transfer into that program
			int ofs = activeEQ.indexOf(new EquipmentType(eqType));
			if (ofs == -1)
				throw securityException("Cannot request transfer to " + eqType);
			
			// Populate the transfer request
			EquipmentType eq = activeEQ.get(ofs);
			txreq = new TransferRequest(p.getID(), eqType);
			txreq.setStatus(_testHistory.canSwitchTo(eq) ? TransferRequest.OK : TransferRequest.PENDING);
			if (eq.getStage() > 1)
				txreq.setRatingOnly(Boolean.valueOf(ctx.getParameter("ratingOnly")).booleanValue());

			// Save the transfer request
			SetTransferRequest wdao = new SetTransferRequest(con);
			wdao.write(txreq);

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
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}