// Copyright 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

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
			
			// Load the airline/source mappings
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> srcs = rsdao.getSources(true);
			Map<ScheduleSource, Collection<Airline>> srcAirlines = rsdao.getSourceAirlines();
			for (ScheduleSourceInfo src : srcs) {
				Collection<Airline> airlines = srcAirlines.getOrDefault(src.getSource(), Collections.emptyList());
				airlines.forEach(al -> src.setLegs(al, 1));
				log.info("Importing " + src.getAirlines() + " from " + src.getSource());
			}
			
			// Start transaction
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			GetRawSchedule rawdao = new GetRawSchedule(con);
			
			// For each source, normalize the replay date back to the day of week
			Set<RawScheduleEntry> entries = new LinkedHashSet<RawScheduleEntry>();
			Map<String, ImportRoute> srcPairs = new HashMap<String, ImportRoute>();
			for (ScheduleSourceInfo srcInfo : srcs) {
				LocalDate effDate = srcInfo.getNextImportDate();
				log.info("Filtering " + srcInfo.getSource() + ", effective " + effDate);
				
				// Purge this source
				int entriesPurged = swdao.purge(srcInfo.getSource());
				log.info("Purged " + entriesPurged + " flight schedule entries from " + srcInfo.getSource().getDescription());
				
				// Load schedule entries, assign legs
				List<RawScheduleEntry> rawEntries = rawdao.load(srcInfo.getSource(), srcInfo.getEffectiveDate()).stream().filter(se -> srcInfo.contains(se.getAirline())).collect(Collectors.toList());
				Collection<RawScheduleEntry> legEntries = ScheduleLegHelper.calculateLegs(rawEntries);
				
				int entriesLoaded = 0;
				for (RawScheduleEntry rse : legEntries) {
					String key = rse.createKey();
					ImportRoute ir = srcPairs.getOrDefault(key, new ImportRoute(rse.getSource(), rse.getAirportD(), rse.getAirportA()));
					if ((ir.getSource() != srcInfo.getSource()) && !rse.getForceInclude()) {
						log.info(ir + " already imported by " + ir.getSource());
						continue;
					}
					
					boolean isAdded = entries.add(rse); ir.setPriority(ir.getSource().ordinal());
					if (isAdded) {
						entriesLoaded++;
						ir.setFlights(ir.getFlights() + 1);
						srcPairs.putIfAbsent(key, ir);
					} else
						log.info(rse.getShortCode() + " already exists [ " + rse.getAirportD().getIATA() + " - " + rse.getAirportA().getIATA() + " ]");
					
					swdao.writeSourceAirlines(srcInfo);
				}
				
				log.info("Loaded " + entriesLoaded + " " + srcInfo.getSource().getDescription() + " schedule entries for " + srcInfo.getEffectiveDate());
			}
			
			// Save the schedule entries
			AirportServiceMap svcMap = new AirportServiceMap();
			for (RawScheduleEntry rse : entries) {
				svcMap.add(rse.getAirline(), rse.getAirportD(), rse.getAirportA());
				swdao.write(rse, false);
			}
			
			// Determine unserviced airports
			SetAirportAirline awdao = new SetAirportAirline(con);
			Collection<Airport> allAirports = SystemData.getAirports().values();
			for (Airport ap : allAirports) {
				Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
				if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
					log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
					ap.setAirlines(svcMap.getAirlineCodes(ap));
					awdao.update(ap, ap.getIATA());
				}
			}
			
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