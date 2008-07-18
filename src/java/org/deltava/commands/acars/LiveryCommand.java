// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;
import java.util.StringTokenizer;

import org.deltava.beans.acars.Livery;
import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle ACARS multi-player livery data. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LiveryCommand extends AbstractFormCommand {

	/**
	 * Callback method called when editing the livery.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the Airline and code
		String id = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		try {
			GetACARSLivery dao = new GetACARSLivery(ctx.getConnection());
			if (!isNew) {
				StringTokenizer tkns = new StringTokenizer(id, "-");
				Airline a = SystemData.getAirline(tkns.nextToken());
				Livery l = dao.get(a, tkns.nextToken());
				if (l == null)
					throw notFoundException("Unknown Livery - " + id);
				
				ctx.setAttribute("livery", l, REQUEST);
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
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving the livery.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		String id = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		try {
			Livery l = null;
			Connection con = ctx.getConnection();
			
			GetACARSLivery dao = new GetACARSLivery(con);
			if (!isNew) {
				StringTokenizer tkns = new StringTokenizer(id, "-");
				Airline a = SystemData.getAirline(tkns.nextToken());
				l = dao.get(a, tkns.nextToken());
				if (l == null)
					throw notFoundException("Unknown Livery - " + id);
			} else {
				
				
			}
			
			// Save other fields
			l.setDescription(ctx.getParameter("desc"));
			l.setDefault(Boolean.valueOf(ctx.getParameter("isDefault")).booleanValue());
			
			// Save the livery
			SetACARSData wdao = new SetACARSData(con);
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setURL("liveries.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}