// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to track hits to a Web Resource.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ResourceJumpCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Resource
			GetResources dao = new GetResources(con);
			Resource r = dao.get(ctx.getID());
			if (r == null)
				throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
			
			// Mark the resource as hit
			SetResource wdao = new SetResource(con);
			wdao.hit(r.getID());
			
			// Save the URL
			result.setURL(r.getURL());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Redirect
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}