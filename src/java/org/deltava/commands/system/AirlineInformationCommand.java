// Copyright 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AirlineInformationAccessControl;

import org.deltava.util.BeanUtils;

/**
 * A Web Site Command to edit Virtual Airline profiles.
 * @author Luke
 * @version 7.4
 * @since 5.0
 */

public class AirlineInformationCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			GetUserData uddao = new GetUserData(con);
			AirlineInformation ai = uddao.get(id); AirlineInformation oai = BeanUtils.clone(ai);
			if (ai == null)
				throw notFoundException("Invalid application code - " + id);
			
			// Check access
			AirlineInformationAccessControl ac = new AirlineInformationAccessControl(ai, ctx);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Cannot edit Virtual Airline profile");
			
			// Update fields
			ai.setCanTransfer(Boolean.valueOf(ctx.getParameter("canTX")).booleanValue());
			ai.setHistoricRestricted(Boolean.valueOf(ctx.getParameter("historicRestrict")).booleanValue());
			ai.setSSL(Boolean.valueOf(ctx.getParameter("hasSSL")).booleanValue());
			ai.setDB(ctx.getParameter("db"));
			ai.setDomain(ctx.getParameter("domain"));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oai, ai);
			AuditLog ae = AuditLog.create(ai, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Save profile
			SetUserData udwdao = new SetUserData(con);
			udwdao.update(ai);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Save request object
			ctx.setAttribute("aInfo", ai, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/aInfoUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			GetUserData uddao = new GetUserData(ctx.getConnection());
			AirlineInformation ai = uddao.get(id);
			if (ai == null)
				throw notFoundException("Invalid application code - " + id);
			
			// Check access
			AirlineInformationAccessControl ac = new AirlineInformationAccessControl(ai, ctx);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Cannot edit Virtual Airline profile");
			
			// Save in request
			readAuditLog(ctx, ai);
			ctx.setAttribute("aInfo", ai, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/aInfoEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			GetUserData uddao = new GetUserData(ctx.getConnection());
			AirlineInformation ai = uddao.get(id);
			if (ai == null)
				throw notFoundException("Invalid application code - " + id);
			
			// Check access
			AirlineInformationAccessControl ac = new AirlineInformationAccessControl(ai, ctx);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot view Virtual Airline profile");
			
			// Save in request
			readAuditLog(ctx, ai);
			ctx.setAttribute("aInfo", ai, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/aInfoRead.jsp");
		result.setSuccess(true);
	}
}