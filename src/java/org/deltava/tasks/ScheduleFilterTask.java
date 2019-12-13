// Copyright 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to filter the day's raw schedule. 
 * @author Luke
 * @version 9.0
 * @since 8.0
 */

public class ScheduleFilterTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public ScheduleFilterTask() {
		super("Schedule Filter", ScheduleFilterTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		try {
			Connection con = ctx.getConnection();
			
			// Load today's entries
			GetRawSchedule rsdao = new GetRawSchedule(con);
			List<RawScheduleEntry> entries = rsdao.load(ScheduleSource.INNOVATA, Instant.now().atZone(ZoneOffset.UTC).toLocalDate());
			Collections.sort(entries, new ScheduleEntryComparator(ScheduleEntryComparator.FLIGHT_DTIME));

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
			
			// Get route pairs
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			SetAirportAirline awdao = new SetAirportAirline(con);
			AirportServiceMap svcMap = sidao.getRoutePairs();
			
			// Determine unserviced airports
			Collection<Airport> allAirports = SystemData.getAirports().values();
			for (Airport ap : allAirports) {
				Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
				if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
					log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
					ap.setAirlines(svcMap.getAirlineCodes(ap));
					awdao.update(ap, ap.getIATA());
				}
			}
			
			// Clear metadata
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.write(aCode + ".schedule.import", Instant.now());
			mdwdao.delete(aCode + ".schedule.effDate");
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Import Complete");
	}
}