// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to delete Equipment Transfer Requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw notFoundException("Invalid Transfer Request - " + ctx.getID());
			
			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Transfer Request");
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(txreq.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot - " + txreq.getID());

			// Get the check ride (if any)
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(txreq.getCheckRideID());
			
			// Use a SQL Transaction
			ctx.startTX();

			// If the Check Ride has not been scored or submitted, delete it
			if ((cr != null) && (cr.getStatus() == Test.NEW)) {
			   SetExam exwdao = new SetExam(con);
			   exwdao.delete(cr);
			   
			   // Set status attribute
			   ctx.setAttribute("checkRideDelete", Boolean.TRUE, REQUEST);
			}

			// Delete the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.delete(usr.getID());

			// Commit the transaction
			ctx.commitTX();

			// Write status attributes to the request
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("txreq", txreq, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/txRequestUpdate.jsp");
		result.setSuccess(true);
	}
}