// Copyright 2005, 2006, 2009, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.Collection;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.EquipmentType;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site command to display Equipment Type profiles.
 * @author Luke
 * @version 7.1
 * @since 1.0
 */

public class EquipmentProfilesCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<EquipmentType> vc = initView(ctx, EquipmentType.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the profiles
			GetEquipmentType dao = new GetEquipmentType(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
			Collection<Integer> IDs = vc.getResults().stream().map(EquipmentType::getCPID).collect(Collectors.toSet());
			
			// Load the Chief Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
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