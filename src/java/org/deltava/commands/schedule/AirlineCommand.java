// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.MapEntry;
import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

import org.gvagroup.common.*;

/**
 * A Web Site Command to update Airline profiles.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class AirlineCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Airline.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the airline code
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);

		Airline a = null;
		try {
			Connection con = ctx.getConnection();
			
			// If we're editing an existing airline, load it
			if (!isNew) {
				GetAirline dao = new GetAirline(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Invalid Airline - " + aCode);
				
				a.setCode(ctx.getParameter("code"));
				a.setName(ctx.getParameter("name"));
			} else {
				a = new Airline(ctx.getParameter("code"), ctx.getParameter("name")); 
			}
			
			// Update the airline from the request
			a.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			a.setApps(ctx.getParameters("airlines"));
			a.setColor(ctx.getParameter("color"));
			a.setCodes(StringUtils.split(ctx.getParameter("altCodes"), "\n"));
			
			// Get the DAO and update the database
			SetSchedule wdao = new SetSchedule(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("airlineCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a, aCode);
				ctx.setAttribute("airlineUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Save the airline in the request
			ctx.setAttribute("airline", a, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Force an airline reload
		EventDispatcher.send(SystemEvent.AirlineReload());

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("airlines.do");
	}

	/**
	 * Callback method called when editing the Airline.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the airline code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);

		// If we're editing an existing airline, load it
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the Airline
				GetAirline dao = new GetAirline(con);
				Airline a = dao.get(aCode);
				if (a != null) {
					ctx.setAttribute("airline", a, REQUEST);
					ctx.setAttribute("altCodes", StringUtils.listConcat(a.getCodes(), "\n"), REQUEST);
				}
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Save airline colors
		ctx.setAttribute("colors", Arrays.asList(MapEntry.COLORS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airlineEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Airline. Redirects to Edit.
	 * @param ctx the Command context
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}