// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
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
			entry.setCanPurge(Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue());
			entry.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());
			entry.setAcademy(Boolean.valueOf(ctx.getParameter("isAcademy")).booleanValue());

			// Parse date/times
			try {
				entry.setTimeD(StringUtils.parseDate(ctx.getParameter("timeD"), "HH:mm"));
				entry.setTimeA(StringUtils.parseDate(ctx.getParameter("timeA"), "HH:mm"));
			} catch (IllegalArgumentException iae) {
				CommandException ce = new CommandException(iae.getMessage());
				ce.setLogStackDump(false);
				throw ce;
			}

			// Write the entry to the database
			SetSchedule wdao = new SetSchedule(con);
			wdao.write(entry, (id != null));

			// Set status attributes
			ctx.setAttribute("scheduleEntry", entry, REQUEST);
			ctx.setAttribute((id == null) ? "isCreate" : "isUpdate", Boolean.TRUE, REQUEST);
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
		
		// Get the Schedule entry
		try {
			Connection con = ctx.getConnection();
			
			if (id != null) {
				GetSchedule dao = new GetSchedule(con);
				ScheduleEntry entry = dao.get(id);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + fCode);
				
				// Load Airports
				Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
				airports.addAll(SystemData.getAirports().values());
				for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
					Airport a = i.next();
					if (!a.getAirlineCodes().contains(entry.getAirline().getCode()))
						i.remove();
				}

				// Save the entry and airports in the request
				ctx.setAttribute("entry", entry, REQUEST);
				ctx.setAttribute("airports", airports, REQUEST);
			}

			//	Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);
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