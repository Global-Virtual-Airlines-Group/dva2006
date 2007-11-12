// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.innovata.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.ftp.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically update the flight Schedule from Innovata.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleImportTask extends Task {

	/**
	 * Initializes the task.
	 */
	public ScheduleImportTask() {
		super("Innovata Update", ScheduleImportTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@SuppressWarnings("unchecked")
	protected void execute(TaskContext ctx) {

		// Load import options
		boolean doPurge = SystemData.getBoolean("schedule.innovata.import.purge");
		boolean canPurge = SystemData.getBoolean("schedule.innovata.import.markCanPurge");
		boolean isHistoric = SystemData.getBoolean("schedule.innovata.import.isHistoric");

		// Get the file name(s) to download and init the cache
		String fileName = SystemData.get("schedule.innovata.download.file");
		FTPCache cache = new FTPCache(SystemData.get("schedule.innovata.cache"));
		cache.setHost(SystemData.get("schedule.innovata.download.host"));
		cache.setCredentials(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));

		// Connect to the FTP server and download the files as needed
		Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		try {
			// If we haven't specified a file name, get the newest file
			if (fileName == null)
				fileName = cache.getNewest("");

			// Download the file
			InputStream is = cache.getFile(fileName);
			FTPDownloadData ftpInfo = cache.getDownloadInfo();
			if (ftpInfo.isCached())
				log.info("Using local copy of " + fileName);
			else
				log.info("Downloaded " + fileName + ", " + ftpInfo.getSize() + " bytes, " + ftpInfo.getSpeed() + " bytes/sec");
			
			// Get the connection
			Connection con = ctx.getConnection();

			// Initialize the DAOs
			GetAircraft acdao = new GetAircraft(con);
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setPrimaryCodes((List) SystemData.getObject("schedule.innovata.primary_codes"));
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setBufferSize(65536);
			ctx.release();

			// Load the schedule data
			dao.load();
			Collection<ScheduleEntry> schedEntries = dao.process();
			Collection<String> codes = new HashSet<String>();
			for (Iterator<ScheduleEntry> si = schedEntries.iterator(); si.hasNext();) {
				ScheduleEntry entry = si.next();
				if (codes.contains(entry.getFlightCode()))
					log.warn("Duplicate flight in " + fileName + " - " + entry.getFlightCode());

				// Update entry attributes
				entry.setCanPurge(canPurge);
				entry.setHistoric(isHistoric);

				// Update internal counters
				codes.add(entry.getFlightCode());
				entries.add(entry);
			}

			// Save error conditions
			SetImportStatus swdao = new SetImportStatus(SystemData.get("schedule.innovata.cache"), "import.status.txt");
			swdao.write(dao.getInvalidAirports(), dao.getInvalidEQ(), dao.getErrorMessages());
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			entries = null;
		} finally {
			ctx.release();
		}
		
		// Return if aborted
		if (entries == null)
			return;

		// Save the entries in the database
		try {
			Connection con = ctx.getConnection();
			SetSchedule dao = new SetSchedule(con);
			ctx.startTX();
			if (doPurge)
				dao.purge(false);

			// Write the entries
			log.info("Saving " + entries.size() + " updated Schedule Entries");
			for (Iterator<ScheduleEntry> i = entries.iterator(); i.hasNext();) {
				ScheduleEntry se = i.next();
				dao.write(se, false);
			}
			
			// Get route pairs
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			AirportServiceMap svcMap = sidao.getRoutePairs();
			
			// Determine unserviced airports
			synchronized (SystemData.class) {
				Collection<Airport> allAirports = SystemData.getAirports().values();
				for (Iterator<Airport> i = allAirports.iterator(); i.hasNext();) {
					Airport ap = i.next();
					Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
					if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
						log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
						ap.setAirlines(svcMap.getAirlineCodes(ap));
						dao.update(ap);
					}
				}
			}

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Log completion
		log.info("Import Complete");
	}
}