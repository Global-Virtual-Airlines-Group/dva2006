// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;
import java.util.List;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site command to display Equipment Type profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentProfilesCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		if (!ctx.isUserInRole("HR"))
			throw securityException("Cannot view Equipment Type profiles");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and execute
			GetEquipmentType dao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", dao.getAll(), REQUEST);
			
			// Get all of the Chief Pilots
			GetPilot pdao = new GetPilot(con);
			List<Pilot> pilots = pdao.getPilotsByRank(Ranks.RANK_CP);
			ctx.setAttribute("chiefPilots", CollectionUtils.createMap(pilots, "ID"), REQUEST);
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