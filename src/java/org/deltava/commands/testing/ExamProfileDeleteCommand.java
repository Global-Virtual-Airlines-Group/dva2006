// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamProfileAccessControl;

/**
 * A Web Site Command to delete an Examination Profile.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

public class ExamProfileDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		String name = (String)ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();

			// Get the profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(name);
			if (ep == null)
				throw notFoundException("Invalid Examination Profile - " + name);
			
			// Check our access
			ExamProfileAccessControl ac = new ExamProfileAccessControl(ctx, ep);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Examination Profile");
			
			// Delete the profile
			SetExamProfile epwdao = new SetExamProfile(con);
			epwdao.delete(ep);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Command
		CommandResult result = ctx.getResult();
		result.setURL("eprofiles.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}