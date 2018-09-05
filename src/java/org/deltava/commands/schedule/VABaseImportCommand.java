// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetVABaseSchedule;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to upload a VABase-format flight schedule.
 * @author Luke
 * @version 8.3
 * @since 8.0
 */

public class VABaseImportCommand extends AbstractCommand {

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
		
		// If we are not uploading a CSV file, then redirect to the JSP
		FileUpload csvData = ctx.getFile("csvData");
		if (csvData == null) {
			ctx.setAttribute("today", Instant.now(), REQUEST);
			CommandResult result = ctx.getResult();
			result.setURL("/jsp/schedule/vaBaseImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the start date
		Instant startTime = parseDateTime(ctx, "start");
		LocalDateTime startDate = (startTime == null) ? null : LocalDateTime.ofInstant(startTime, ZoneOffset.UTC);
		
		try {
			Connection con = ctx.getConnection();
			GetVABaseSchedule idao = new GetVABaseSchedule(csvData.getInputStream());
			
			// Load aircraft codes
			GetAircraft acdao = new GetAircraft(con);
			idao.setStartDate(startDate);
			idao.setAircraft(acdao.getAircraftTypes());
			idao.setAirlines(SystemData.getAirlines().values());
			
			// Build the collection by day
			Map<DayOfWeek, List<RawScheduleEntry>> dayEntries = new HashMap<DayOfWeek, List<RawScheduleEntry>>(); 
			List.of(DayOfWeek.values()).forEach(dow -> dayEntries.put(dow, new ArrayList<RawScheduleEntry>()));
			ScheduleEntryComparator scmp = new ScheduleEntryComparator(ScheduleEntryComparator.DTIME);
			
			// Load the schedule and Group based on flight code
			idao.process().stream().map(RawScheduleEntry.class::cast).forEach(rse -> dayEntries.get(rse.getDay()).add(rse));

			// Sort by departure time
			Collection<RawScheduleEntry> updatedLegs = new ArrayList<RawScheduleEntry>();
			for (List<RawScheduleEntry> entries : dayEntries.values()) {
				Map<String, Collection<RawScheduleEntry>> sortedEntries = new TreeMap<String, Collection<RawScheduleEntry>>();	
				for (RawScheduleEntry rse : entries) {
					String k = rse.getFlightCode();
					Collection<RawScheduleEntry> bucket = sortedEntries.get(k);
					if (bucket == null) {
						bucket = new TreeSet<RawScheduleEntry>(scmp);
						sortedEntries.put(k, bucket);
					}
					
					bucket.add(rse);
				}

				// Set the leg number
				for (Collection<RawScheduleEntry> bucket : sortedEntries.values()) {
					int leg = 1;
					for (RawScheduleEntry rse : bucket) {
						rse.setLeg(leg);
						leg++;
						updatedLegs.add(rse);
					}
				}
			}
			
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw();
			for (RawScheduleEntry se : updatedLegs)
				swdao.writeRaw(se);
			
			// Check if anything has been imported
			GetRawSchedule rsdao = new GetRawSchedule(con);
			ctx.setAttribute("hasRawSchedule", Boolean.valueOf(rsdao.hasEntries(Instant.now())), REQUEST);
			
			// Write metadata
			String aCode = SystemData.get("airline.code").toLowerCase();
			SetMetadata mdwdao = new SetMetadata(con);
			mdwdao.write(aCode + ".schedule.vaBaseDate", startTime);
			
			// Set status attributes
			ctx.setAttribute("msgs", idao.getErrorMessages(), REQUEST);
			ctx.setAttribute("eqTypes", idao.getInvalidEQ(), REQUEST);
			ctx.setAttribute("airlines", idao.getInvalidAirlines(), REQUEST);
			ctx.setAttribute("airports", idao.getInvalidAirports(), REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/vaBaseStatus.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}