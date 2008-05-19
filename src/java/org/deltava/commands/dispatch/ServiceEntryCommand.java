// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.sql.Connection;

import org.deltava.beans.UserData;
import org.deltava.beans.acars.DispatchScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchScheduleAccessControl;

/**
 * A Web Site Command to handle ACARS Dispatcher schedule entries.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class ServiceEntryCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		boolean isNew = (ctx.getID() == 0);
		try {
			DispatchScheduleEntry dse = null;
			if (!isNew) {
				Connection con = ctx.getConnection();
				
				// Get the entry
				GetDispatchCalendar dao = new GetDispatchCalendar(con);
				dse = dao.get(ctx.getID());
				if (dse == null)
					throw notFoundException("Invalid Dispatch Schedule entry - " + ctx.getID());
				
				// Get the Dispatcher
				GetPilot pdao = new GetPilot(con);
				GetUserData uddao = new GetUserData(con);
				UserData ud = uddao.get(dse.getAuthorID());
				
				// Save in request
				ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
				ctx.setAttribute("entry", dse, REQUEST);
			}

			// Check our access
			DispatchScheduleAccessControl ac = new DispatchScheduleAccessControl(ctx, dse);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Cannot edit Dispatch Schedule entry");
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/schedEntryEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the entry
			GetDispatchCalendar dao = new GetDispatchCalendar(con);
			DispatchScheduleEntry dse = dao.get(ctx.getID());
			if (dse == null)
				throw notFoundException("Invalid Dispatch Schedule entry - " + ctx.getID());
			
			// Save the entry
			ctx.setAttribute("entry", dse, REQUEST);
			
			// Load the Dispatcher
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(dse.getAuthorID());
			ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/schedEntry.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when saving the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the entry
			DispatchScheduleEntry dse = null;
			if (isNew)
				dse = new DispatchScheduleEntry(ctx.getUser().getID());
			else {
				GetDispatchCalendar dao = new GetDispatchCalendar(con);
				dse = dao.get(ctx.getID());
				if (dse == null)
					throw notFoundException("Unknown Dispatch Schedule entry - " + ctx.getID());
			}
			
			// Update common parameters
			dse.setStartTime(parseDateTime(ctx, "start"));
			dse.setEndTime(parseDateTime(ctx, "end"));
			dse.setComments(ctx.getParameter("comments"));
			
			// Check our access
			DispatchScheduleAccessControl ac = new DispatchScheduleAccessControl(ctx, dse);
			ac.validate();
			boolean access = isNew ? ac.getCanCreate() : ac.getCanEdit();
			if (!access)
				throw securityException("Cannot create/edit Dispatch Schedule entry");
			
			// Write to the database
			SetDispatchCalendar wdao = new SetDispatchCalendar(con);
			wdao.write(dse);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("dspcalendar.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}