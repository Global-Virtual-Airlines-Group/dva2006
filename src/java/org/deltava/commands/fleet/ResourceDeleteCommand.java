// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ResourceAccessControl;

/**
 * A Web Site Command to delete a Web Resource link.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class ResourceDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Resource
			GetResources dao = new GetResources(con);
			Resource r = dao.get(ctx.getID());
			if (r == null)
				throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
			
			// Check our access
			ResourceAccessControl ac = new ResourceAccessControl(ctx, r);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Web Resource");
			
			// Delete the resource
			SetResource wdao = new SetResource(con);
			wdao.delete(r.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Command
		CommandResult result = ctx.getResult();
		result.setURL("resources.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}