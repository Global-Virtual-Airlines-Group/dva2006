// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.GetAircraft;
import org.deltava.dao.DAOException;
import org.deltava.dao.file.innovata.*;

import org.deltava.util.ftp.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to download and import Innovata LLC schedule data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InnovataDownloadCommand extends ScheduleImportCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@SuppressWarnings("unchecked")
	public void execute(CommandContext ctx) throws CommandException {

		// Get the file name to download and init the cache
		String fileName = SystemData.get("schedule.innovata.download.file");
		FTPCache cache = new FTPCache(SystemData.get("schedule.innovata.cache"));
		cache.setHost(SystemData.get("schedule.innovata.download.host"));
		cache.setCredentials(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));

		// Connect to the FTP server and download the files as needed
		try {
			Collection<String> msgs = new ArrayList<String>();
			Collection<String> codes = new HashSet<String>();
			Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
			
			// If we haven't specified a file name, get the newest file
			if (fileName == null)
				fileName = cache.getNewest("");

			// Download the files
			boolean isCached = false;
			InputStream is = cache.getFile(fileName);

			// Get download information
			FTPDownloadData ftpInfo = cache.getDownloadInfo();
			isCached |= ftpInfo.isCached();
			if (ftpInfo.isCached()) {
				msgs.add("Using local copy of " + fileName);
			} else {
				msgs.add("Downloaded " + fileName + ", " + ftpInfo.getSize() + " bytes, " + ftpInfo.getSpeed() + " bytes/sec");
			}

			// Initialize the DAO
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			GetFullSchedule dao = new GetFullSchedule(is);
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setPrimaryCodes((List) SystemData.getObject("schedule.innovata.primary_codes"));
			dao.setAirlines(SystemData.getAirlines().values());
			dao.setAirports(SystemData.getAirports().values());
			ctx.release();

			// Load the schedule data
			dao.load();
			Collection<ScheduleEntry> schedEntries = dao.process();
			for (Iterator<ScheduleEntry> si = schedEntries.iterator(); si.hasNext();) {
				ScheduleEntry entry = si.next();
				if (codes.contains(entry.getFlightCode()))
					msgs.add("Duplicate flight in " + fileName + " - " + entry.getFlightCode());

				codes.add(entry.getFlightCode());
				entries.add(entry);
			}
			
			// Save the error messages
			msgs.addAll(dao.getErrorMessages());
			
			// Save the status
			SetImportStatus swdao = new SetImportStatus(SystemData.get("schedule.innovata.cache"), "import.status.txt");
			swdao.write(dao.getInvalidAirports(), dao.getInvalidEQ(), msgs);

			// Save the cache status
			ctx.setAttribute("innovataCache", Boolean.valueOf(isCached), REQUEST);

			// Save the data in the session
			ctx.setAttribute("entries", entries, SESSION);
			ctx.setAttribute("schedType", SCHED_TYPES[INNOVATA], SESSION);
			ctx.setAttribute("errors", msgs, SESSION);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/flightSave.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}