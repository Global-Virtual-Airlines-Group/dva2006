// Copyright 2005, 2006, 2007, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
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
 * @version 7.0
 * @since 1.0
 */

public class ScheduleEntryCommand extends AbstractFormCommand {
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();

	/**
	 * Callback method called when saving the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the old flight ID
		String fCode = (String) ctx.getCmdParameter(ID, null);
		ScheduleEntry id = FlightCodeParser.parse(fCode);

		// Check our access
		ScheduleAccessControl ac = new ScheduleAccessControl(ctx);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException("Cannot modify Flight Schedule");
		
		// Get command results
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();
			
			// Get schedule effective date
			GetMetadata mddao = new GetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			Instant effDate = mddao.getDate(aCode + ".schedule.effDate").truncatedTo(ChronoUnit.DAYS);
			ctx.setAttribute("effectiveDate", effDate, REQUEST);

			// Get the existing entry or create a new one
			GetSchedule dao = new GetSchedule(con);
			dao.setEffectiveDate(effDate);
			ScheduleEntry entry = null;
			if (id != null) {
				entry = dao.get(id);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + fCode);
			} else {
				Airline a = SystemData.getAirline(ctx.getParameter("airline"));
				int fNumber = StringUtils.parse(ctx.getParameter("flightNumber"), 1);
				entry = new ScheduleEntry(a, fNumber, StringUtils.parse(ctx.getParameter("flightLeg"), 1));
			}
			
			// Load the departure airport
			Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
			if (aD == null)
				aD = SystemData.getAirport(ctx.getParameter("airportDCode"));
			
			// Load the arrival airport
			Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
			if (aA == null)
				aA = SystemData.getAirport(ctx.getParameter("airportACode"));

			// Update the entry
			entry.setEquipmentType(ctx.getParameter("eqType"));
			entry.setCanPurge(!Boolean.valueOf(ctx.getParameter("dontPurge")).booleanValue());
			entry.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());
			entry.setAcademy(Boolean.valueOf(ctx.getParameter("isAcademy")).booleanValue());
			
			// Parse times
			LocalTime dt = LocalTime.parse(ctx.getParameter("timeD"), _tf);
			LocalTime at = LocalTime.parse(ctx.getParameter("timeA"), _tf);
			entry.setTimeD(LocalDateTime.ofInstant(effDate, ZoneOffset.UTC).plusSeconds(dt.toSecondOfDay()));
			entry.setTimeA(LocalDateTime.ofInstant(effDate, ZoneOffset.UTC).plusSeconds(at.toSecondOfDay()));
			
			// If either airport is null, redirect to the edit page
			if ((aD == null) || (aA == null)) {
				ctx.setMessage("Unknown Airport(s)");
				
				// Get Airports
				Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
				GetAirport adao = new GetAirport(con);
				airports.addAll(adao.getByAirline(entry.getAirline(), null));

				// Save the entry and airports in the request
				ctx.setAttribute("entry", entry, REQUEST);
				ctx.setAttribute("airports", airports, REQUEST);

				//	Get aircraft types
				GetAircraft acdao = new GetAircraft(con);
				ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);

				// Release and redirect
				ctx.release();
				result.setURL("/jsp/schedule/schedEntry.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Set the airports
			entry.setAirportD(aD);
			entry.setAirportA(aA);
			
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the flight ID
		String fCode = (String) ctx.getCmdParameter(ID, null);
		ScheduleEntry id = FlightCodeParser.parse(fCode);
		
		// Get the Schedule entry
		try {
			Connection con = ctx.getConnection();
			
			// Get schedule effective date
			GetMetadata mddao = new GetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			Instant effDate = mddao.getDate(aCode + ".schedule.effDate");
			ctx.setAttribute("effectiveDate", effDate, REQUEST);
			
			if (id != null) {
				GetSchedule dao = new GetSchedule(con);
				dao.setEffectiveDate(effDate);
				ScheduleEntry entry = dao.get(id);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + fCode);
				
				// Get Airports
				Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
				GetAirport adao = new GetAirport(con);
				airports.addAll(adao.getByAirline(entry.getAirline(), null));

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
	 * Callback method called when reading the profile.
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}