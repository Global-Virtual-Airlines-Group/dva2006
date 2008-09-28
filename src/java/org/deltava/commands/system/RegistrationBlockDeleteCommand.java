// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.beans.system.RegistrationBlock;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete a Registration Block entry.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RegistrationBlockDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the block entry
			GetSystemData dao = new GetSystemData(con);
			RegistrationBlock rb = dao.getBlock(ctx.getID());
			if (rb == null)
				throw notFoundException("Invalid Registration Block entry - " + ctx.getID());
			
			// Delete the entry
			SetSystemData wdao = new SetSystemData(con);
			wdao.deleteBlock(rb.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("regblocks.do");
		result.setSuccess(true);
	}
}