// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.Ranks;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentAccessControl;

/**
 * A Web Site Command to update Equipment Type profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentSaveCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the equipment type
		String eqType = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (eqType == null);

		// Check our access
		EquipmentAccessControl access = new EquipmentAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot modify Equipment Profile");

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the existing equipment type profile
			GetEquipmentType rdao = new GetEquipmentType(con);
			EquipmentType eq = isNew ? new EquipmentType(ctx.getParameter("eqType")) : rdao.get(eqType);
			
			// Update the equipment type profile from the request
			eq.setCPID(Integer.parseInt(ctx.getParameter("cp")));
			eq.setStage(Integer.parseInt(ctx.getParameter("stage")));
			eq.setActive("1".equals(ctx.getParameter("active")));
			eq.setRanks(ctx.getRequest().getParameterValues("ranks"));
			
			// Update primary/secondary ratings
			String[] pRatings = ctx.getRequest().getParameterValues("pRatings");
			String[] sRatings = ctx.getRequest().getParameterValues("sRatings");
			eq.setRatings(pRatings, sRatings);

			// Update examination names
			eq.setExamName(Ranks.RANK_FO, ctx.getParameter("examFO"));
			eq.setExamName(Ranks.RANK_C, ctx.getParameter("examC"));
			
			// Get the DAO and write the equipment type to the database
			SetEquipmentType wdao = new SetEquipmentType(con);
			if (isNew) {
				wdao.create(eq);
				ctx.setAttribute("isCreated", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(eq);
			}
			
			// Save the equipment program in the request
			ctx.setAttribute("eqType", eq, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Redirect to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/admin/eqUpdate.jsp");
		result.setSuccess(true);
	}
}