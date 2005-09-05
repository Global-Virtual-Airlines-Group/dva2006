// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to reject Equipment Profile transfer requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferRejectCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the Message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw new CommandException("Invalid Transfer Request - " + ctx.getID());

			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanReject())
				throw securityException("Cannot reject Transfer Request");

			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(txreq.getID());
			if (usr == null)
				throw new CommandException("Invalid Pilot - " + txreq.getID());

			// Make a status update
			StatusUpdate upd = new StatusUpdate(txreq.getID(), StatusUpdate.COMMENT);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Transfer Request to " + txreq.getEquipmentType() + " program Rejected");
			
			// Save the pilot for the message context
			mctxt.addData("pilot", usr);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("XFERREJECT"));

			// Use a SQL Transaction
			ctx.startTX();

			// Save the status update
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(upd);

			// Delete the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.delete(usr.getID());

			// Commit the transaction
			ctx.commitTX();

			// Write status attributes to the request
			ctx.setAttribute("isReject", Boolean.valueOf(true), REQUEST);
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("txreq", txreq, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(usr);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/txRequestUpdate.jsp");
		result.setSuccess(true);
	}
}