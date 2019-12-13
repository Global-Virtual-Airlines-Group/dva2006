// Copyright 2006, 2007, 2008, 2009, 2010, 2012, 2015, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;
import java.sql.Connection;
import java.time.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to download and import Innovata LLC schedule data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class InnovataDownloadCommand extends ScheduleImportCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void execute(CommandContext ctx) throws CommandException {

		// Get the file name to download and init the cache
		String fileName = SystemData.get("schedule.innovata.file");

		// Calculate replay date
		String dt = SystemData.get("schedule.innovata.import.replayDate");
		LocalDateTime replayDate = StringUtils.isEmpty(dt) ? null : LocalDateTime.ofInstant(StringUtils.parseInstant(dt, "MM/dd/yyyy"), ZoneOffset.UTC);
		if (replayDate != null) {
			LocalDateTime now = LocalDateTime.now();
			int daysToAdjust = now.get(ChronoField.DAY_OF_WEEK) - 1;
			replayDate = replayDate.plusDays(daysToAdjust);
		}
		
		// Connect to the FTP server and download the files as needed
		try (InputStream fis = new FileInputStream(fileName)) {
			Collection<String> msgs = new ArrayList<String>();
			Collection<String> codes = new HashSet<String>();
			Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
			InputStream is = fis;
			if (fileName.endsWith(".zip")) {
				@SuppressWarnings("resource")
				ZipInputStream zis = new ZipInputStream(fis);
				zis.getNextEntry();
				is = zis;
			}
			
			// Get the connection
			Connection con = ctx.getConnection();

			// Initialize the DAO
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setAirlines(adao.getActive().values());
			dao.setMainlineCodes((List<String>) SystemData.getObject("schedule.innovata.primary_codes"));
			dao.setCodeshareCodes((List<String>) SystemData.getObject("schedule.innovata.codeshare_codes"));
			dao.setBufferSize(131072);
			ctx.release();

			// Load the schedule data
			dao.load();
			Collection<RawScheduleEntry> schedEntries = dao.process();
			for (RawScheduleEntry entry : schedEntries) {
				if (codes.contains(entry.getFlightCode()))
					msgs.add("Duplicate flight in " + fileName + " - " + entry.getFlightCode());

				codes.add(entry.getFlightCode());
				entries.add(entry);
			}
			
			// Save the error messages
			msgs.addAll(dao.getStatus().getErrorMessages());
			
			// Save the status
			SetImportStatus swdao = new SetImportStatus(SystemData.get("schedule.innovata.cache"), "import.status.txt");
			swdao.write(dao.getStatus());
			
			// Save schedule metadata
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.write(aCode + ".schedule.import", Instant.now());
			if (replayDate != null)
				mdwdao.write(aCode + ".schedule.effDate", replayDate.toInstant(ZoneOffset.UTC));
			else
				mdwdao.delete(aCode + ".schedule.effDate");

			// Save the cache status
			ctx.setAttribute("innovataCache", Boolean.TRUE, REQUEST);
			ctx.setAttribute("replayDate", replayDate, REQUEST);

			// Save the data in the session
			ctx.setAttribute("entries", entries, SESSION);
			ctx.setAttribute("errors", msgs, SESSION);
		} catch (IOException | DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/flightSave.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}