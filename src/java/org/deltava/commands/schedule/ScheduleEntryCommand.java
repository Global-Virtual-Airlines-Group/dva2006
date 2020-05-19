// Copyright 2005, 2006, 2007, 2009, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.Collections;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Schedule entries.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleEntryCommand extends AbstractFormCommand {
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("MM/dd[/yyyy]").parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear()).toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("[H]H:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();

	/**
	 * Callback method called when saving the schedule entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the source/line
		ScheduleSource src = EnumUtils.parse(ScheduleSource.class, ctx.getParameter("src"), ScheduleSource.MANUAL);
		int srcLine = StringUtils.parse(ctx.getParameter("srcLine"), -1);
		boolean isNew = (srcLine > 0);

		try {
			Connection con = ctx.getConnection();
			
			// Get the existing entry or create a new one
			GetRawSchedule dao = new GetRawSchedule(con);
			RawScheduleEntry entry = null;
			if (!isNew) {
				entry = dao.get(src, srcLine);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + src.getDescription() + " Line " + srcLine);
			} else {
				Airline a = SystemData.getAirline(ctx.getParameter("airline"));
				int fNumber = StringUtils.parse(ctx.getParameter("flightNumber"), 1);
				entry = new RawScheduleEntry(a, fNumber, StringUtils.parse(ctx.getParameter("flightLeg"), 1));
				entry.setSource(src);
			}
			
			// Check our access
			ScheduleAccessControl ac = new ScheduleAccessControl(ctx, entry);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Cannot modify Flight Schedule entry");
			
			// Load the airports
			entry.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
			entry.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
			
			// Load start/end dates
			entry.setStartDate(LocalDate.parse(ctx.getParameter("startDate"), _df));
			entry.setEndDate(LocalDate.parse(ctx.getParameter("endDate"), _df));
			if (entry.getEndDate().isAfter(entry.getStartDate()))
				entry.setEndDate(entry.getEndDate().plusYears(1));

			// Update the entry
			entry.setEquipmentType(ctx.getParameter("eqType"));
			entry.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());
			entry.setAcademy(Boolean.valueOf(ctx.getParameter("isAcademy")).booleanValue());
			entry.setForceInclude(Boolean.valueOf(ctx.getParameter("forceInclude")).booleanValue());
			
			// Parse times
			entry.setTimeD(LocalDateTime.of(entry.getStartDate(), LocalTime.parse(ctx.getParameter("timeD"), _tf)));
			entry.setTimeA(LocalDateTime.of(entry.getStartDate(), LocalTime.parse(ctx.getParameter("timeA"), _tf)));
			
			// Get a line number if manual entry
			if ((src == ScheduleSource.MANUAL) && (srcLine <= 0)) {
				GetRawScheduleInfo ridao = new GetRawScheduleInfo(con);
				entry.setLineNumber(ridao.getNextManualEntryLine());
			}
			
			// Write the entry to the database
			SetSchedule wdao = new SetSchedule(con);
			wdao.writeRaw(entry);

			// Set status attributes
			ctx.setAttribute("scheduleEntry", entry, REQUEST);
			ctx.setAttribute(isNew ? "isCreate" : "isUpdate", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
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

		// Get the source/line
		String id = (String) ctx.getCmdParameter(ID, "-"); int pos = id.indexOf('-');
		ScheduleSource src = EnumUtils.parse(ScheduleSource.class, id.substring(0, pos), ScheduleSource.MANUAL);
		int srcLine = StringUtils.parse(id.substring(pos + 1), -1);
		
		try {
			Connection con = ctx.getConnection();
			if (srcLine > -1) {
				GetRawSchedule dao = new GetRawSchedule(con);
				ScheduleEntry entry = dao.get(src, srcLine);
				if (entry == null)
					throw notFoundException("Invalid Schedule Entry - " + src.getDescription() + " Line " + srcLine);

				ctx.setAttribute("entry", entry, REQUEST);
				ctx.setAttribute("airportsD", Collections.singletonList(entry.getAirportD()), REQUEST);
				ctx.setAttribute("airportsA", Collections.singletonList(entry.getAirportA()), REQUEST);
			}

			//	Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Build JSON object to store historical airlines
		JSONObject jho = new JSONObject();
		SystemData.getAirlines().values().forEach(a -> jho.put(a.getCode(), a.getHistoric()));
		ctx.setAttribute("historicAL", jho, REQUEST);

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