// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.schedule.RawScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save VABase schedule data for the current day into the flight schedule.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class VABaseFilterCommand extends AbstractCommand {
	
	/**
	 * Departure time schedule entry comparator.
	 */
	static class DepartureTimeComparator implements Comparator<RawScheduleEntry> {

		@Override
		public int compare(RawScheduleEntry rs1, RawScheduleEntry rs2) {

			int tmpResult = rs1.compareTo(rs2);
			return (tmpResult == 0) ? rs1.getTimeD().toInstant().compareTo(rs2.getTimeD().toInstant()) : tmpResult;
		}
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Flight Schedule data");

		try {
			Connection con = ctx.getConnection();
			
			// Load today's entries
			GetRawSchedule rsdao = new GetRawSchedule(con);
			List<RawScheduleEntry> entries = rsdao.load(Instant.now());
			Collections.sort(entries, new DepartureTimeComparator());
			
			// Calculate the leg numbers
			RawScheduleEntry lastE = null;
			for (RawScheduleEntry rse : entries) {
				if (lastE == null) {
					lastE = rse;
					continue;
				}
				
				if (rse.getAirline().equals(lastE.getAirline()) && (rse.getFlightNumber() == lastE.getFlightNumber()))
					rse.setLeg(lastE.getLeg() + 1);
				
				lastE = rse;
			}
			
			// Save the entries
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purge(false);
			for (RawScheduleEntry rse : entries)
				swdao.write(rse, true);
			
			// Clear metadata
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.delete(aCode + ".schedule.effDate");
			
			ctx.setAttribute("entriesLoaded", Integer.valueOf(entries.size()), REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isFilter", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/vaBaseStatus.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}