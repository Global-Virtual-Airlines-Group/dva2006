// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.sql.Connection;
import java.time.LocalDate;

import org.deltava.beans.econ.EliteLevel;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update Elite status levels.
 * @author Luke
 * @version 10.0
 * @since 9.2
 */

public class EliteLevelCommand extends AbstractFormCommand {

	/**
     * Callback method called when saving the Elite level.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check our access
		EliteAccessControl ac = new EliteAccessControl(ctx);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException("Cannot edit Elite levels");
		
		// Load from the request
		EliteLevel lvl = new EliteLevel(StringUtils.parse(ctx.getParameter("year"), 0), ctx.getParameter("name"));
		lvl.setLegs(StringUtils.parse(ctx.getParameter("legs"), 0));
		lvl.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		lvl.setPoints(StringUtils.parse(ctx.getParameter("pts"), 0));
		lvl.setColor(StringUtils.parse("0x" + ctx.getParameter("color"), 0));
		lvl.setBonusFactor(StringUtils.parse(ctx.getParameter("bonus"), 0) / 100.0f + 1);
		lvl.setTargetPercentile(StringUtils.parse("targetPct", 1));
		lvl.setVisible(Boolean.valueOf(ctx.getParameter("isVisible")).booleanValue());
		
		// Save the bean
		try {
			SetElite ewdao = new SetElite(ctx.getConnection());
			ewdao.write(lvl);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status attributes
		ctx.setAttribute("level", lvl, REQUEST);
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/econ/eliteLevelUpdate.jsp");
		result.setSuccess(true);
	}

	/**
     * Callback method called when editing the Elite level.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check our access
		EliteAccessControl ac = new EliteAccessControl(ctx);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException("Cannot edit Elite levels");
		
		// Parse the ID and optionally load the bean
		String id = ctx.getParameter("id");
		if (id != null) {
			int pos = id.indexOf('-');
			String name = id.substring(0, pos);
			int year = StringUtils.parse(id.substring(pos + 1), LocalDate.now().getYear());

			try {
				Connection con = ctx.getConnection();
				GetElite edao = new GetElite(con);
				EliteLevel lvl = edao.get(name, year, ctx.getDB());
				if (lvl == null)
					throw notFoundException("Cannot load Elite level " + name + " for " + year);

				// Save in request
				ctx.setAttribute("lvl", lvl, REQUEST);
				ctx.setAttribute("pilotCount", Integer.valueOf(edao.getPilotCount(lvl)), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteLevel.jsp");
		result.setSuccess(true);
	}

	/**
     * Not implemented - calls @see EliteLevelCommand#execEdit(CommandContext)
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}