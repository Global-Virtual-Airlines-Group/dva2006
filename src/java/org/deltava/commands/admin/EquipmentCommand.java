package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.Ranks;
import org.deltava.beans.EquipmentType;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentAccessControl;

/**
 * A Web Site Command to edit Equipment Type profiles. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class EquipmentCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the equipment type
		String eqType = (String) ctx.getCmdParameter(Command.ID, null);
		
		// Check our access
		EquipmentAccessControl access = new EquipmentAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw new CommandSecurityException("Cannot modify Equipment Profile");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Chief Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("chiefPilots", pdao.getPilotsByRank(Ranks.RANK_CP), REQUEST);
			
			// Get the DAO and execute
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = (eqType == null) ? null : eqdao.get(eqType);
			
			// Get the Examination names
			GetExamProfiles exdao = new GetExamProfiles(con);
			ctx.setAttribute("exams", exdao.getExamProfiles(), REQUEST);
			
			// Save the equipment profile and access controller
			ctx.setAttribute("eqType", eq, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/eqProfile.jsp");
		result.setSuccess(true);
	}
}