// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.dao.file.GetJSONSchedule;

import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to load Raw Schedule data from another Golgotha instance. This is typically done to transfer API-based
 * raw schedules that are subject to a usage limit from the Production environment into non-Production environments. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class RawScheduleTransferTask extends Task {

	/**
	 * Creates the Task.
	 */
	public RawScheduleTransferTask() {
		super("Raw Schedule Trasnfer", RawScheduleTransferTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Get the sources and the host
		String host = SystemData.get("schedule.xfer.master");
		Collection<?> srcNames = (Collection<?>) SystemData.getObject("schedule.xfer.srcs");
		Collection<ScheduleSource> srcs = srcNames.stream().map(Object::toString).map(sn -> EnumUtils.parse(ScheduleSource.class, sn, null)).filter(Objects::nonNull).collect(Collectors.toSet());
		
		// Load each soruce
		Map<ScheduleSource, Collection<RawScheduleEntry>> results = new HashMap<ScheduleSource, Collection<RawScheduleEntry>>();
		LocalDate ld = LocalDate.now().plusDays(14); String dt = StringUtils.format(ld, "yyyy-MM-dd");
		for (ScheduleSource src : srcs) {
			log.info("Loading {} entries for {} from {}", src.name(), dt, host);
			String url = String.format("https://%s/rawschedxfer.ws?src=%s&date=%s", host, src.name(), dt);
			try {
				TaskTimer tt = new TaskTimer();
				GetURL urldao = new GetURL(url, "/dev/null");
				urldao.setCompression(Compression.GZIP, Compression.DEFLATE);
				urldao.setAuthentication("Golgotha", SystemData.get("secret.key.golgotha"));
				urldao.setConnectTimeout(3500);
				urldao.setReadTimeout(25000);
				byte[] data = urldao.load();
				log.info("{} loaded in {}ms", url, Long.valueOf(tt.stop()));
				
				// Parse the schedule
				try (InputStream is = new ByteArrayInputStream(data)) {
					GetJSONSchedule jsdao = new GetJSONSchedule(src, is); // Don't need to load aircraft since eqType has already been parsed
					jsdao.setAirlines(SystemData.getAirlines());
					Collection<RawScheduleEntry> entries = jsdao.process();
					log.info("Loaded {} entries from {} for {}", Integer.valueOf(entries.size()), host, src.name());
					results.put(src, entries);
				}
			} catch (IOException | DAOException de) {
				log.atError().withThrowable(de).log("Error loading {} - {}", url, de.getMessage());
			}
		}
		
		// Save the entries
		try {
			Connection con = ctx.getConnection();
			SetSchedule swdao = new SetSchedule(con);
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(con);
			for (Map.Entry<ScheduleSource, Collection<RawScheduleEntry>> me : results.entrySet()) {
				ctx.startTX();
				
				// Get the starting line number and write
				Collection<RawScheduleEntry> entries = me.getValue();
				int ln = rsdao.getNextLine(me.getKey());
				for (RawScheduleEntry rse : entries) {
					rse.setLineNumber(ln++);
					swdao.writeRaw(rse, false);
				}
					
				log.info("Wrote {} entries for {}", Integer.valueOf(entries.size()), me.getKey().name());
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());			
		} finally {
			ctx.release();
		}
		
		log.info("Complete");
	}
}