// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.innovata.GetSchedule;

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

		// Determine the date of the locally cached file
		String fileName = SystemData.get("schedule.innovata.download.file");
		File cf = new File(SystemData.get("schedule.innovata.cache"), fileName);
		Date ldt = cf.isFile() ? new Date(cf.lastModified()) : null;
		
		// Connect to the FTP server and download the file
		InputStream is = null;
		FTPConnection con = new FTPConnection(SystemData.get("schedule.innovata.download.host"));
		try {
			con.connect(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));
			log.info("Connected to " + con.getClient().getRemoteHost());
			
			// Check the remote file date
			Date rdt = con.getTimestamp("", fileName);
			if (rdt == null) {
				log.warn("Cannot find " + fileName + "!");
				throw notFoundException("Cannot find FTP schedule data");
			}
				 
			// Download the file
			Collection<String> msgs = new ArrayList<String>();
			if ((ldt == null) || (ldt.before(rdt))) {
				long now = System.currentTimeMillis();
				log.info("Downloading " + cf.getName() + ", local=" + ldt + ", remote=" + rdt);
				msgs.add("Downloading " + cf.getName() + ", local=" + ldt + ", remote=" + rdt + " from " + con.getClient().getRemoteHost());
				is = con.get(fileName, cf);
				cf.setLastModified(rdt.getTime());
				log.info("Download Complete. " + is.available() + " bytes, " + (System.currentTimeMillis() - now) + " ms");
				msgs.add("Download Complete. " + is.available() + " bytes, " + (System.currentTimeMillis() - now) + " ms");
			} else {
				log.info("Using local copy " + cf.getAbsolutePath());
				msgs.add("Using local copy " + cf.getAbsolutePath());
				ctx.setAttribute("innovataCache", Boolean.TRUE, REQUEST);
				is = new FileInputStream(cf);
			}
			
			// If we have no input stream, abort
			if (is == null)
				throw notFoundException("No schedule data found");
			
			// Initialize the DAO
			GetSchedule dao = new GetSchedule(is);
			dao.setAirlines(SystemData.getAirlines().values());
			dao.setAirports(SystemData.getAirports().values());
			dao.setPartners(_codeShareInfo);
			
			// Load the schedule data
			dao.load();
			Collection<ScheduleEntry> entries = dao.process();
			msgs.addAll(dao.getErrorMessages());

			// Save the data in the session
			ctx.setAttribute("entries", entries, SESSION);
			ctx.setAttribute("schedType", SCHED_TYPES[INNOVATA], SESSION);
			ctx.setAttribute("errors", msgs, SESSION);
		} catch (IOException ie) {
			log.warn(ie.getMessage());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			con.close();
			try {
				if (is != null)
					is.close();
			} catch (IOException ie) {
				// empty
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/flightSave.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}