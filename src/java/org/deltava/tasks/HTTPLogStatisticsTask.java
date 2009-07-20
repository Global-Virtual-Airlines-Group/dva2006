// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

import org.deltava.beans.stats.HTTPStatistics;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to aggregate HTTP log statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class HTTPLogStatisticsTask extends Task {

	private static final DateFormat _df = new SimpleDateFormat("yyyy-MMM-dd");

	private class HTTPLogFilter implements FileFilter {

		private Calendar _startTime;

		HTTPLogFilter() {
			super();
			_startTime = Calendar.getInstance();
			_startTime.set(Calendar.HOUR_OF_DAY, 0);
			_startTime.set(Calendar.MINUTE, 0);
			_startTime.set(Calendar.SECOND, 0);
			_startTime.add(Calendar.SECOND, -1);
		}

		public boolean accept(File f) {

			// Ensure that we start with httpd-access
			String name = f.getName();
			if (!name.startsWith(SystemData.get("log.http.format")))
				return false;

			try {
				String ext = name.substring(name.lastIndexOf('.') + 1);
				Date d = new Date(Long.parseLong(ext) * 1000);
				return d.before(_startTime.getTime());
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * Initializes the task.
	 */
	public HTTPLogStatisticsTask() {
		super("HTTP Log Statistics", HTTPLogStatisticsTask.class);
	}

	/**
	 * Executes the Task. This will parse through HTTP server log entries and aggregate the statistics.
	 */
	protected void execute(TaskContext ctx) {

		// Get the HTTP log path
		File logPath = new File(SystemData.get("path.httplog"));
		if (!logPath.exists()) {
			log.error("Cannot find HTTP log path " + logPath.getAbsolutePath());
			return;
		}

		// Initialize the Log parser
		LogParser parser = initParser(SystemData.get("log.http.parser"));
		if (parser == null)
			return;

		// Look for logs
		Collection<File> files = Arrays.asList(logPath.listFiles(new HTTPLogFilter()));
		for (Iterator<File> i = files.iterator(); i.hasNext();) {
			File f = i.next();
			HTTPStatistics stats = parser.parseLog(f);
			if (stats != null) {
				log.info("Updating statistics for " + StringUtils.format(stats.getDate(), "MM/dd/yyyy"));
				try {
					SetSystemLog dao = new SetSystemLog(ctx.getConnection());
					dao.write(stats);
					
					// Convert to a GZIP'd file
					File gzf = new File(SystemData.get("log.http.archive"), _df.format(stats.getDate()) + ".gz");
					try {
						InputStream in = new FileInputStream(f);
						GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzf), 32768);
						byte[] buf = new byte[65536];
						int bytesRead = in.read(buf);
						while (bytesRead != -1) {
							out.write(buf, 0, bytesRead);
							bytesRead = in.read(buf);
						}
						
						// Close stuff and delete the raw log file
						in.close();
						out.close();
						f.delete();
					} catch (IOException ie) {
						log.error("Error compressing " + f.getName(), ie);
					}
				} catch (DAOException de) {
					log.error("Error saving statistics for " + StringUtils.format(stats.getDate(), "MM/dd/yyyy")
							+ " - " + de.getMessage(), de);
				} finally {
					ctx.release();
				}
			}
		}

		// Log completion
		log.info("Processing Complete");
	}

	/**
	 * Helper method to init the log parser.
	 */
	private LogParser initParser(String className) {
		try {
			Class<?> c = Class.forName(className);
			return (LogParser) c.newInstance();
		} catch (Exception e) {
			log.error("Cannot load " + className + " - " + e.getClass().getName(), e);
		}

		return null;
	}
}