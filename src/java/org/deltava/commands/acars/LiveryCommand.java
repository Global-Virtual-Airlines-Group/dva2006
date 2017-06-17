// Copyright 2008, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.acars.Livery;
import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle ACARS multi-player livery data. 
 * @author Luke
 * @version 7.4
 * @since 2.2
 */

public class LiveryCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when editing the livery.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the Airline and code
		String id = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		try {
			Connection con = ctx.getConnection();
			GetACARSLivery dao = new GetACARSLivery(con);
			if (!isNew) {
				StringTokenizer tkns = new StringTokenizer(id, "-");
				Airline a = SystemData.getAirline(tkns.nextToken());
				Livery l = dao.get(a, tkns.nextToken());
				if (l == null)
					throw notFoundException("Unknown Livery - " + id);
				
				ctx.setAttribute("livery", l, REQUEST);
				readAuditLog(ctx, l);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save airlines
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/liveryEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the livery.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving the livery.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		String id = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		try {
			Livery l = null; Livery ol = null;
			Connection con = ctx.getConnection();
			
			GetACARSLivery dao = new GetACARSLivery(con);
			if (!isNew) {
				StringTokenizer tkns = new StringTokenizer(id, "-");
				Airline a = SystemData.getAirline(tkns.nextToken());
				l = dao.get(a, tkns.nextToken()); ol = BeanUtils.clone(l);
				if (l == null)
					throw notFoundException("Unknown Livery - " + id);
			} else 
				l = new Livery(SystemData.getAirline(ctx.getParameter("airline")), ctx.getParameter("code"));
			
			// Save other fields
			l.setDescription(ctx.getParameter("desc"));
			l.setDefault(Boolean.valueOf(ctx.getParameter("isDefault")).booleanValue());
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(ol, l);
			AuditLog ae = AuditLog.create(l, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Write audit log
			writeAuditLog(ctx, ae);
			
			// Save the livery and commit
			SetACARSData wdao = new SetACARSData(con);
			wdao.write(l);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setURL("liveries.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}