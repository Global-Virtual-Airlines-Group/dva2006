// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.innovata.GetSchedule;

import org.deltava.util.*;
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
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the file name(s) to download and init the cache
		List<String> fileNames = StringUtils.split(SystemData.get("schedule.innovata.download.file"), ",");
		FTPCache cache = new FTPCache(SystemData.get("schedule.innovata.cache"));
		cache.setCredentials(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));
		
		// Connect to the FTP server and download the files as needed
		try {
			Collection<String> msgs = new ArrayList<String>();
			Collection<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
			
			// Download the files
			InputStream is = null;
			boolean isCached = false;
			for (Iterator<String> i = fileNames.iterator(); i.hasNext(); ) {
				String fileName = i.next();
				is = cache.getFile(fileName);
				
				// Get download infor
				FTPDownloadData ftpInfo = cache.getDownloadInfo();
				isCached |= ftpInfo.isCached();
				if (ftpInfo.isCached()) {
					msgs.add("Using local copy of " + fileName);
				} else {
					msgs.add("Downloaded " + fileName + ", " + ftpInfo.getSize() + " bytes, " + ftpInfo.getSpeed() + " bytes/sec");
				}
				
				// Initialize the DAO
				GetSchedule dao = new GetSchedule(is);
				dao.setAirlines(SystemData.getAirlines().values());
				dao.setAirports(SystemData.getAirports().values());
				dao.setPartners(_codeShareInfo);

				// Load the schedule data
				dao.load();
				entries.addAll(dao.process());
				msgs.addAll(dao.getErrorMessages());
			}
			
			// Save the cache status
			ctx.setAttribute("innovataCache", Boolean.valueOf(isCached), REQUEST);

			// Save the data in the session
			ctx.setAttribute("entries", entries, SESSION);
			ctx.setAttribute("schedType", SCHED_TYPES[INNOVATA], SESSION);
			ctx.setAttribute("errors", msgs, SESSION);
		} catch (DAOException de) {
			throw new CommandException(de);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/flightSave.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}