// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.innovata.*;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.*;
import org.deltava.util.ftp.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically update the flight Schedule from Innovata.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleImportTask extends DatabaseTask {

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
	protected void execute() {

		// Load import options
		boolean doPurge = SystemData.getBoolean("schedule.innovata.import.purge");
		boolean canPurge = SystemData.getBoolean("schedule.innovata.import.markCanPurge");
		boolean isHistoric = SystemData.getBoolean("schedule.innovata.import.isHistoric");

		// Get the file name(s) to download and init the cache
		String fileName = SystemData.get("schedule.innovata.download.file");
		FTPCache cache = new FTPCache(SystemData.get("schedule.innovata.cache"));
		cache.setHost(SystemData.get("schedule.innovata.download.host"));
		cache.setCredentials(SystemData.get("schedule.innovata.download.user"), SystemData
				.get("schedule.innovata.download.pwd"));

		// Connect to the FTP server and download the files as needed
		AirportServiceMap svcMap = new AirportServiceMap();
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
				log.info("Downloaded " + fileName + ", " + ftpInfo.getSize() + " bytes, " + ftpInfo.getSpeed()
						+ " bytes/sec");

			// Initialize the DAO
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setPrimaryCodes((List) SystemData.getObject("schedule.innovata.primary_codes"));
			dao.setAirlines(SystemData.getAirlines().values());
			dao.setAirports(SystemData.getAirports().values());
			dao.setBufferSize(65536);

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
				svcMap.add(entry.getAirline(), entry.getAirportD());
				svcMap.add(entry.getAirline(), entry.getAirportA());
			}

			// Save error conditions
			SetImportStatus swdao = new SetImportStatus(SystemData.get("schedule.innovata.cache"), "import.status.txt");
			swdao.write(dao.getInvalidAirports(), dao.getInvalidEQ(), dao.getErrorMessages());
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return;
		}

		// Determine unserviced airports
		Collection<Airport> updatedAirports = new HashSet<Airport>();
		Collection<Airport> allAirports = SystemData.getAirports().values();
		synchronized (SystemData.class) {
			for (Iterator<Airport> i = allAirports.iterator(); i.hasNext();) {
				Airport ap = i.next();
				if (CollectionUtils.hasDelta(ap.getAirlineCodes(), svcMap.getAirlineCodes(ap))) {
					log.info("Updating airlines for " + ap);
					ap.setAirlines(svcMap.getAirlineCodes(ap));
					updatedAirports.add(ap);
				}
			}
		}

		// Save the entries in the database
		try {
			startTX();
			SetSchedule dao = new SetSchedule(_con);
			if (doPurge)
				dao.purge(false);

			// Write the entries
			log.info("Saving " + entries.size() + " updated Schedule Entries");
			for (Iterator<ScheduleEntry> i = entries.iterator(); i.hasNext();) {
				ScheduleEntry se = i.next();
				dao.write(se, false);
			}

			// Update unserviced airports
			log.info("Updating " + updatedAirports.size() + " Airport airline information entries");
			for (Iterator<Airport> i = updatedAirports.iterator(); i.hasNext();) {
				Airport ap = i.next();
				dao.update(ap);
			}

			// Commit the transaction
			commitTX();
		} catch (DAOException de) {
			rollbackTX();
			log.error(de.getMessage(), de);
		}

		// Log completion
		log.info("Import Complete");
	}
}