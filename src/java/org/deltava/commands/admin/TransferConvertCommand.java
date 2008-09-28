// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to toggle the additional ratings flag on a Transfer Request.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class TransferConvertCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest tx = txdao.get(ctx.getID());
			if (tx == null)
				throw notFoundException("No Transfer Request for Pilot " + ctx.getID());
			
			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, tx);
			access.validate();
			if (!access.getCanApprove())
				throw securityException("Cannot toggle Transfer type");
			
			// Toggle the flag
			tx.setRatingOnly(!tx.getRatingOnly());
			
			// Write the transfer
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.update(tx);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward back to the transfer request
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("txreqview", null, ctx.getID());
		result.setSuccess(true);
	}
}