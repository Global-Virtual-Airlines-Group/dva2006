// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to update lifetime Elite status level definitions.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetimeLevelCommand extends AbstractAuditFormCommand {

	/**
     * Callback method called when saving the lifetime Elite level.
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
		EliteLifetime el = new EliteLifetime(ctx.getParameter("name"));
		el.setCode(ctx.getParameter("code"));
		el.setLegs(StringUtils.parse(ctx.getParameter("legs"), 0));
		el.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		
		int yr = EliteScorer.getStatusYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			GetElite dao = new GetElite(con);
			EliteLifetime oel = dao.getLifetime(ctx.getParameter("id"), ctx.getDB());
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oel, el);
			AuditLog ae = AuditLog.create(el, delta, ctx.getUser().getID());
			
			// Get levels for lifetime status
			String lvlName = ctx.getParameter("level");
			TreeSet<EliteLevel> lvls = dao.getLevels(yr);
			@SuppressWarnings("unlikely-arg-type")
			EliteLevel lvl = lvls.stream().filter(lv -> lv.equals(lvlName)).findFirst().orElse(null);
			if (lvl == null)
				throw notFoundException("Unknown Elite level - " + lvlName);

			el.setLevel(lvl);
			
			// Start transaction
			ctx.startTX();
			
			// Write the bean
			SetElite ewdao = new SetElite(con);
			ewdao.write(el);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteLevelUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
     * Callback method called when editing the lifetime Elite level.
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

		String name = ctx.getParameter("id");
		int yr = EliteScorer.getStatusYear(Instant.now());
		try {
			GetElite dao = new GetElite(ctx.getConnection());
			if (name != null) {
				EliteLifetime el = dao.getLifetime(name, ctx.getDB());
				if (el == null)
					throw notFoundException("Unknown lifetime Elite level - " + name);
			
				readAuditLog(ctx, el);
				ctx.setAttribute("lvl", el, REQUEST);
				ctx.setAttribute("pilotCount", Integer.valueOf(dao.getPilotCount(el)), REQUEST);
			}
			
			ctx.setAttribute("statusLevels", dao.getLevels(yr), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteLTLevel.jsp");
		result.setSuccess(true);
	}

	/**
     * Not implemented - calls @see EliteLifetimeLevelCommand#execEdit(CommandContext)
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}