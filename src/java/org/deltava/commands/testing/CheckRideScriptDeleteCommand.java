// Copyright 2006, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.testing.EquipmentRideScript;
import org.deltava.beans.testing.EquipmentRideScriptKey;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentRideScriptAccessControl;

/**
 * A Web Site Command to delete Check Ride scripts.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class CheckRideScriptDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		EquipmentRideScriptKey key = EquipmentRideScriptKey.parse((String) ctx.getCmdParameter(ID, "?!!?"));
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the script
			GetExamProfiles dao = new GetExamProfiles(con);
			EquipmentRideScript cs = dao.getScript(key);
			if (cs == null)
				throw notFoundException("Invalid Equipment Type - " + key.getEquipmentType());

			// Check our access
			EquipmentRideScriptAccessControl ac = new EquipmentRideScriptAccessControl(ctx, cs);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Check Ride script");

			// Save the script bean in the request
			ctx.setAttribute("script", cs, REQUEST);

			// Get the write DAO and delete the script
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.delete(key);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setSuccess(true);
	}
}