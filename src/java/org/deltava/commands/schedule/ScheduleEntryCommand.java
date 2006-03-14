// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.text.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.FlightCodeParser;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Schedule entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleEntryCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the old flight ID
		String fCode = (String) ctx.getCmdParameter(ID, null);
		ScheduleEntry id = FlightCodeParser.parse(fCode);

		// Check our access
		ScheduleAccessControl ac = new ScheduleAccessControl(ctx);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException("Cannot modify Flight Schedule");

		try {
			Connection con = ctx.getConnection();

			// Get the existing entry or create a new one
			GetSchedule dao = new GetSchedule(con);
			ScheduleEntry entry = null;
			if (id != null) {
				entry = dao.get(id);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + fCode);
			} else {
				// Create a new entry
				Airline a = SystemData.getAirline(ctx.getParameter("airline"));
				try {
					int fNumber = Integer.parseInt(ctx.getParameter("flightNumber"));
					int fLeg = Integer.parseInt(ctx.getParameter("flightLeg"));
					entry = new ScheduleEntry(a, fNumber, fLeg);
				} catch (NumberFormatException nfe) {
					CommandException ce = new CommandException("Invalid Flight data " + nfe.getMessage());
					ce.setLogStackDump(false);
					throw ce;
				}
			}

			// Update the entry
			entry.setEquipmentType(ctx.getParameter("eqType"));
			entry.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
			entry.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
			entry.setPurge(Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue());
			entry.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());

			// Parse date/times
			DateFormat df = new SimpleDateFormat("HH:mm");
			try {
				entry.setTimeD(df.parse(ctx.getParameter("timeD")));
				entry.setTimeA(df.parse(ctx.getParameter("timeA")));
			} catch (ParseException pe) {
				CommandException ce = new CommandException(pe.getMessage());
				ce.setLogStackDump(false);
				throw ce;
			}

			// Write the entry to the database
			SetSchedule wdao = new SetSchedule(con);
			if (id == null) {
				wdao.write(entry, false);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.write(entry, true);
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			}

			// Set status attribute
			ctx.setAttribute("scheduleEntry", entry, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the flight ID
		String fCode = (String) ctx.getCmdParameter(ID, null);
		ScheduleEntry id = FlightCodeParser.parse(fCode);

		try {
			// Get the Schedule entry
			if (id != null) {
				Connection con = ctx.getConnection();
				GetSchedule dao = new GetSchedule(con);
				ScheduleEntry entry = dao.get(id);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + fCode);

				// Save the entry in the request
				ctx.setAttribute("entry", entry, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/schedEntry.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the profile. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}