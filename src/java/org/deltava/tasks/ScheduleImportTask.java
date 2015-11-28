// Copyright 2006, 2007, 2009, 2010, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.EMailAddress;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.innovata.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.ftp.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically update the flight Schedule from Innovata.
 * @author Luke
 * @version 6.3
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
	@Override
	@SuppressWarnings("unchecked")
	protected void execute(TaskContext ctx) {

		// Load import options
		boolean doPurge = SystemData.getBoolean("schedule.innovata.import.purge");
		boolean canPurge = SystemData.getBoolean("schedule.innovata.import.markCanPurge");
		boolean isHistoric = SystemData.getBoolean("schedule.innovata.import.isHistoric");
		
		// Calculate replay date
		String dt = SystemData.get("schedule.innovata.import.replayDate");
		Date replayDate = StringUtils.isEmpty(dt) ? null : StringUtils.parseDate(dt, "MM/dd/yyyy");
		if (replayDate != null) {
			Calendar now = CalendarUtils.getInstance(null, true);
			int daysToAdjust = now.get(Calendar.DAY_OF_WEEK) - 1;
			Calendar rd = CalendarUtils.getInstance(replayDate, true, daysToAdjust);
			replayDate = rd.getTime();
		}

		// Get the file name(s) to download and init the cache
		String fileName = SystemData.get("schedule.innovata.download.file");
		FTPCache cache = new FTPCache(SystemData.get("schedule.innovata.cache"));
		cache.setHost(SystemData.get("schedule.innovata.download.host"));
		cache.setCredentials(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));

		// Connect to the FTP server and download the files as needed
		Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		try {
			// If we haven't specified a file name, get the newest file
			if ((fileName == null) || fileName.contains("*"))
				fileName = cache.getNewest("", FileUtils.fileFilter(fileName, ".zip"));

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
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setEffectiveDate(CalendarUtils.getInstance(replayDate, true).getTime());
			dao.setMainlineCodes((List<String>) SystemData.getObject("schedule.innovata.primary_codes"));
			dao.setCodeshareCodes((List<String>) SystemData.getObject("schedule.innovata.codeshare_codes"));
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setAirlines(adao.getActive().values());
			dao.setBufferSize(131072);
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
			swdao.write(dao.getInvalidAirlines(), dao.getInvalidAirports(), dao.getInvalidEQ(), dao.getErrorMessages());
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			entries = null;
		} finally {
			ctx.release();
		}
		
		// Return if insufficient records loaded
		int minEntries = SystemData.getInt("schedule.innovata.import.minEntries", 100);
		if (entries.size() < minEntries) {
			log.warn("Insufficient Schedule entries downloaded, minimum " + minEntries + ", got " + entries.size());
			return;
		}

		// Save the entries in the database
		Exception saveError = null; Collection<EMailAddress> notifyUsers = new ArrayList<EMailAddress>();
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();
			
			// Get notification template and pilots
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("SCHEDERROR"));
			notifyUsers.addAll(pdao.getByRole("Schedule", SystemData.get("airline.db")));
			
			// Purge the schedule
			SetSchedule dao = new SetSchedule(con);
			ctx.startTX();
			if (doPurge)
				dao.purge(false);

			// Write the entries
			log.info("Saving " + entries.size() + " updated Schedule Entries");
			for (ScheduleEntry se : entries)
				dao.write(se, false);
			
			// Get route pairs
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			AirportServiceMap svcMap = sidao.getRoutePairs();
			
			// Determine unserviced airports
			Collection<Airport> allAirports = SystemData.getAirports().values();
			for (Airport ap : allAirports) {
				Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
				if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
					log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
					ap.setAirlines(svcMap.getAirlineCodes(ap));
					dao.update(ap, ap.getIATA());
				}
			}
			
			// Save schedule metadata
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.write(aCode + ".schedule.import", new Date());
			if (replayDate != null)
				mdwdao.write(aCode + ".schedule.effDate", replayDate);
			else
				mdwdao.delete(aCode + ".schedule.effDate");

			ctx.commitTX();
		} catch (DAOException de) {
			saveError = de;
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Log an error
		if ((saveError != null) && (!notifyUsers.isEmpty()) && (mctxt.getTemplate() != null)) {
			StringWriter sw = new StringWriter();
			saveError.printStackTrace(new PrintWriter(sw));
			mctxt.addData("error", saveError);
			mctxt.addData("stackTrace", sw.toString());
			
			// Send notification
			EMailAddress addr = Mailer.makeAddress(SystemData.get("airline.mail.webmaster"), SystemData.get("airline.name"));
			Mailer m = new Mailer(addr);
			m.setContext(mctxt);
			m.send(notifyUsers);
		}
		
		log.info("Import Complete");
	}
}