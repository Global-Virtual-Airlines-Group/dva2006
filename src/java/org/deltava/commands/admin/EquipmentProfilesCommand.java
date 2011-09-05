// Copyright 2005, 2006, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site command to display Equipment Type profiles.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class EquipmentProfilesCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			GetEquipmentType dao = new GetEquipmentType(ctx.getConnection());
			ctx.setAttribute("eqTypes", dao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/eqProfileList.jsp");
		result.setSuccess(true);
	}
}