// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2017, 2019, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.hr.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to request a transfer to a different Equipment program.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class TransferRequestCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// Get the Pilot object
		Pilot p = ctx.getUser();
		ctx.setAttribute("pilot", p, REQUEST);
		
		// Determine if we are requesting an additional rating only
		boolean isRating = "rating".equals(ctx.getCmdParameter(OPERATION, null));
		ctx.setAttribute("isRating", Boolean.valueOf(isRating), REQUEST);

		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();

			// Initialize the testing history helper
			TestingHistoryHelper testHistory = initTestHistory(p, con);

			// Get the active Equipment Profiles and determine what we can switch to
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Map<String, EquipmentType> activeEQ = CollectionUtils.createMap(eqdao.getAvailable(SystemData.get("airline.code")), EquipmentType::getName);
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
				ctx.setAttribute("availableSims", testHistory.getSimulators(90), REQUEST);

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
			txreq.setAircraftType(ctx.getParameter("acType"));
			if (StringUtils.isEmpty(txreq.getAircraftType()))
				txreq.setAircraftType(eq.getName());
			
			txreq.setSimulator(Simulator.fromName(ctx.getParameter("sim"), Simulator.UNKNOWN));
			if (!eq.getOwner().getCode().equals(SystemData.get("airline.code")))
				txreq.setRatingOnly(true);
			else
				txreq.setRatingOnly(Boolean.parseBoolean(ctx.getParameter("ratingOnly")));
			
			// If we're attempting to switch to our current program, request ratings only
			if (txreq.getEquipmentType().equals(p.getEquipmentType()))
				txreq.setRatingOnly(true);
			
			// Set status to approved if we can transfter
			try {
				testHistory.canSwitchTo(eq);
				txreq.setStatus(TransferStatus.COMPLETE);
			} catch (IneligibilityException ie) {
				txreq.setStatus(TransferStatus.PENDING);
			}
			
			// Set mailer variables
			mctxt.addData("user", p);
			mctxt.addData("eqType", eq);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("XFERNEW"));

			// Save the transfer request
			SetTransferRequest wdao = new SetTransferRequest(con);
			wdao.create(txreq, eq.getOwner().getDB());
			
			// Store the transfer request in the request
			ctx.setAttribute("txReq", txreq, REQUEST);
			mctxt.addData("txReq", txreq);
			
			// Send message
			GetPilot pdao = new GetPilot(con);
			Mailer m = new Mailer(p);
			m.setContext(mctxt);
			pdao.getPilotsByEQ(eq, null, true, Rank.ACP).forEach(m::setCC);
			m.send(pdao.get(eq.getCPID()));
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