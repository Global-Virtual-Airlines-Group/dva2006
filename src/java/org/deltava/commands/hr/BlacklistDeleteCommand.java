// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;

import org.deltava.beans.system.BlacklistEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete a login/registration blacklist entry.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class BlacklistDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		String addr = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Find the entry
			GetSystemData dao = new GetSystemData(con);
			BlacklistEntry be = dao.getBlacklist(addr);
			if (be == null)
				throw notFoundException(addr + " not in Blacklist");
			
			// Dete the entry
			SetSystemData wdao = new SetSystemData(con);
			wdao.deleteBlacklist(be.getCIDR().getNetworkAddress());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL(ctx.getRequest().getHeader("Referer"));
		result.setSuccess(true);
	}
}