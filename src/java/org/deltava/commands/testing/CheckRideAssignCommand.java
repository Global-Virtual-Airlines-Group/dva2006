// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EquipmentType;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to assign Check Rides.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw new CommandException("Invalid Transfer Request - " + ctx.getID());

			// Check our access level
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanAssignRide())
				throw new CommandSecurityException("Cannot assign Check Ride");

			// Get the Pilot profile
			GetPilot dao = new GetPilot(con);
			p = dao.get(txreq.getID());
			if (p == null)
				throw new CommandException("Invalid Pilot - " + txreq.getID());

			// Get the Equipment Type for the check ride
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(ctx.getParameter("eqType"));
			if (eq == null)
				throw new CommandException("Invalid Equipment Program - " + ctx.getParameter("eqType"));

			// Generate the checkride
			CheckRide cr = new CheckRide(eq.getName() + " Check Ride");
			cr.setDate(new java.util.Date());
			cr.setPilotID(ctx.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(Test.NEW);
			cr.setStage(eq.getStage());
			cr.setComments(ctx.getParameter("comments"));
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eq);
			
			// Use a SQL Transaction
			ctx.startTX();

			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Update the transfer request
			txreq.setCheckRideID(cr.getID());
			txreq.setStatus(TransferRequest.PENDING);

			// Save the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.write(txreq);
			
			// Commit the transaction
			ctx.commitTX();

			// Save the checkride in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("checkRide", cr, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(p);

		// Update status for the JSP
		ctx.setAttribute("isAssign", Boolean.valueOf(true), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}