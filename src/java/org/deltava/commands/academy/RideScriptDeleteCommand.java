// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.AcademyRideScript;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyRideScriptAccessControl;

/**
 * A Web Site Command to delete a Flight Academy Check Ride script. 
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class RideScriptDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			String id = (String) ctx.getCmdParameter(ID, "");
			Connection con = ctx.getConnection();
			
			// Load the script
			GetAcademyCertifications acdao = new GetAcademyCertifications(con);
			AcademyRideScript sc = acdao.getScript(id);
			if (sc == null)
				throw notFoundException("Academy Check Ride script not found - " + id);
			
			// Check our access
			AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, sc);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Academy Check Ride script");
			
			// Delete the script
			SetAcademyCertification awdao = new SetAcademyCertification(con);
			awdao.deleteScript(id);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("arscripts.do");
		result.setSuccess(true);
	}
}