// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;

import org.deltava.beans.stats.Accomplishment;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AccomplishmentAccessControl;

/**
 * A Web Site Command to delete Accomplishment profiles.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentDeleteCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Accomplishment
			GetAccomplishment dao = new GetAccomplishment(con);
			Accomplishment a = dao.get(ctx.getID());
			if (a == null)
				throw notFoundException("Invalid Accomplishment profile - " + ctx.getID());
			
			// Check our access
			AccomplishmentAccessControl ac = new AccomplishmentAccessControl(ctx, a);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Accomplishment profile");
			
			// Delete the profile
			SetAccomplishment wdao = new SetAccomplishment(con);
			wdao.delete(a.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("accomplishments.do");
		result.setSuccess(true);
	}
}