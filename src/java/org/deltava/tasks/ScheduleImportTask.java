// Copyright 2006, 2007, 2009, 2010, 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;

import org.deltava.beans.EMailAddress;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.innovata.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically update the flight Schedule from Innovata.
 * @author Luke
 * @version 7.2
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
		LocalDateTime replayDate = StringUtils.isEmpty(dt) ? null : LocalDateTime.ofInstant(StringUtils.parseInstant(dt, "MM/dd/yyyy"), ZoneOffset.UTC);
		if (replayDate != null) {
			LocalDateTime now = LocalDateTime.now();
			int daysToAdjust = now.get(ChronoField.DAY_OF_WEEK) - 1;
			replayDate = replayDate.plusDays(daysToAdjust);
		}

		// Get the file name(s) to download and init the cache
		String fileName = SystemData.get("schedule.innovata.file");

		// Connect to the FTP server and download the files as needed
		final Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		try (InputStream fis = new FileInputStream(fileName)) {
			InputStream is = fis;
			if (fileName.endsWith(".zip")) {
				@SuppressWarnings("resource")
				ZipInputStream zis = new ZipInputStream(fis);
				zis.getNextEntry();
				is = zis;
			}
			
			Connection con = ctx.getConnection();

			// Initialize the DAOs
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setEffectiveDate(replayDate);
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
			for (ScheduleEntry entry : schedEntries) {
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
		} catch (IOException | DAOException de) {
			log.error(de.getMessage(), de);
			entries.clear();
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
			mdwdao.write(aCode + ".schedule.import", Instant.now());
			if (replayDate != null)
				mdwdao.write(aCode + ".schedule.effDate", replayDate.toInstant(ZoneOffset.UTC));
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