// Copyright 2017, 2019, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to filter the day's raw schedule. 
 * @author Luke
 * @version 11.1
 * @since 8.0
 */

public class ScheduleFilterTask extends Task {
	
	/**
	 * Initializes the Task.
	 */
	public ScheduleFilterTask() {
		super("Schedule Filter", ScheduleFilterTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Load the airline/source mappings
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> srcInfos = rsdao.getSources(true, ctx.getDB());
			Collection<ScheduleSourceHistory> srcs = srcInfos.stream().map(inf -> new ScheduleSourceHistory(inf)).collect(Collectors.toCollection(ArrayList::new));
			Map<ScheduleSource, Collection<Airline>> srcAirlines = rsdao.getSourceAirlines(); // TODO: These are the loaded airlines!
			for (ScheduleSourceInfo src : srcs) {
				Collection<Airline> airlines = srcAirlines.getOrDefault(src.getSource(), Collections.emptyList());
				src.setAirlines(airlines);
				log.info("Importing {} from {}", src.getAirlines(), src.getSource());
			}
			
			// TODO: Are we ensuring that previously imported airlines are not getting restored?
			
			// Start transaction
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			GetRawSchedule rawdao = new GetRawSchedule(con);
			
			// For each source, normalize the replay date back to the day of week
			final LocalDate today = LocalDate.now();
			Set<RawScheduleEntry> entries = new LinkedHashSet<RawScheduleEntry>();
			Map<String, ImportRoute> srcPairs = new HashMap<String, ImportRoute>();
			for (ScheduleSourceHistory srcInfo : srcs) {
				TaskTimer tt = new TaskTimer();
				LocalDate effDate = srcInfo.getNextImportDate();
				log.info("Filtering {}, effective {}", srcInfo.getSource(), effDate);
				srcInfo.setEffectiveDate(effDate.atStartOfDay().toInstant(ZoneOffset.UTC));
				srcInfo.setImportDate(Instant.now());
				srcInfo.setAutoImport(true);
				srcInfo.setPurged(true);
				
				// Purge this source
				int entriesPurged = swdao.purge(srcInfo.getSource());
				log.info("Purged {} flight schedule entries from {}", Integer.valueOf(entriesPurged), srcInfo.getSource().getDescription());
				
				// Load schedule entries, assign legs
				List<RawScheduleEntry> rawEntries = rawdao.load(srcInfo.getSource(), srcInfo.getEffectiveDate()).stream().filter(se -> srcInfo.contains(se.getAirline())).collect(Collectors.toList());
				Collection<RawScheduleEntry> legEntries = ScheduleLegHelper.calculateLegs(rawEntries); rawEntries.clear();
				for (RawScheduleEntry rse : legEntries) {
					String key = rse.createKey();
					ImportRoute ir = srcPairs.getOrDefault(key, new ImportRoute(rse.getSource(), rse.getAirportD(), rse.getAirportA()));
					if ((ir.getSource() != srcInfo.getSource()) && ir.hasAirline(rse.getAirline()) && !rse.getForceInclude()) {
						log.debug("{} already imported by {}", ir, ir.getSource());
						srcInfo.skip();
						continue;
					}
					
					// Adjust to the effective date
					rse.setTimeD(LocalDateTime.of(srcInfo.getEffectiveDate(), rse.getTimeD().toLocalTime()));
					rse.setTimeA(LocalDateTime.of(srcInfo.getEffectiveDate(), rse.getTimeA().toLocalTime()));
					if (rse.adjustForDST(today))
						srcInfo.adjust();
					
					boolean isAdded = entries.add(rse); ir.setPriority(ir.getSource().ordinal());
					if (isAdded) {
						srcInfo.addLegs(rse.getAirline(), 1);
						ir.addEntry(rse);
						srcPairs.putIfAbsent(key, ir);
					} else {
						srcInfo.skip();
						log.info("{} already exists [ {} - {} ]", rse.getShortCode(), rse.getAirportD().getIATA(), rse.getAirportA().getIATA());
					}
				}

				srcInfo.setTime((int) tt.stop());
				srcInfo.setActive(srcInfo.getLegs() > 1);
				swdao.writeSourceAirlines(srcInfo);
				log.info("Loaded {} ({} skipped) {} schedule entries for {} in {}ms", Integer.valueOf(srcInfo.getLegs()), Integer.valueOf(srcInfo.getSkipped()), srcInfo.getSource().getDescription(), srcInfo.getEffectiveDate(), Integer.valueOf(srcInfo.getTime()));
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
					log.info("Updating {} new codes = {}, was {}", ap.getName(), newAirlines, ap.getAirlineCodes());
					ap.setAirlines(svcMap.getAirlineCodes(ap));
					awdao.update(ap, ap.getIATA());
				}
			}
			
			ctx.commitTX();
			CacheManager.invalidate("ScheduleSource", true);
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Import Complete");
	}
}