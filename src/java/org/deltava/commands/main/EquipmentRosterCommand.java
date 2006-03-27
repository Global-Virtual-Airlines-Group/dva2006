// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilots within a particular Equipment Type program.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentRosterCommand extends AbstractViewCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Determine if we show inactive pilots
		boolean showAll = Boolean.valueOf(ctx.getParameter("showAll")).booleanValue();
		
		// Load the view context
		ViewContext vc = initView(ctx, showAll ? 150 : SystemData.getInt("html.table.viewSize"));
		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetPilot dao = new GetPilot(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getPilotsByEQ(ctx.getParameter("eqType"), !showAll));
			
			// Get the Equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("showAll", Boolean.valueOf(showAll), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/eqRoster.jsp");
		result.setSuccess(true);
	}
}