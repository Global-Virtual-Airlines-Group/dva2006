// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.*;

import org.deltava.beans.schedule.Aircraft;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to handle Aircraft profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AircraftCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);

		try {
			Connection con = ctx.getConnection();
			
			// If we're editing an existing aircraft, load it
			Aircraft a = null;
			if (!isNew) {
				GetAircraft dao = new GetAircraft(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);

				a.setName(ctx.getParameter("name"));
			} else
				a = new Aircraft(ctx.getParameter("name"));

			// Update the aircraft from the request
			a.setRange(StringUtils.parse(ctx.getParameter("range"), 0));
			a.setIATA(StringUtils.split(ctx.getParameter("iataCodes"), "\n"));
			
			// Get the DAO and update the database
			SetSchedule wdao = new SetSchedule(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("aircraftCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a);
				ctx.setAttribute("aircraftUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Save the aircraft in the request
			ctx.setAttribute("aircraft", a, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("aircraftList.do");
	}

	/**
	 * Callback method called when editing the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);

		// If we're editing an existing aircraft, load it
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();
			
				// Get the DAO and the Aircraft
				GetAircraft dao = new GetAircraft(con);
				Aircraft a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);
				
				ctx.setAttribute("aircraft", a, REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Aircraft profile. <i>NOT IMPLEMENTED </i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}