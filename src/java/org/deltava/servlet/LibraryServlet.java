// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2015, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.fleet.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;

import org.deltava.security.SecurityContext;
import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.ControllerException;
import org.deltava.util.TaskTimer;
import org.deltava.util.URLParser;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;

/**
 * A servlet to serve Fleet/Document/File/Video Library files.
 * @author Luke
 * @version 11.3
 * @since 1.0
 */

public class LibraryServlet extends GenericServlet {

	private static final Logger log = LogManager.getLogger(LibraryServlet.class);
	private static final int BUFFER_SIZE = 102400;

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "Fleet Library Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Processes HTTP GET requests for Fleet Library resources.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Get the resource we want
		URLParser url = new URLParser(req.getRequestURI());
		LibraryEntry entry = null;

		// Get the connection pool
		ConnectionPool<Connection> pool = SystemData.getJDBCPool();
		Connection c = null;
		try {
			c = pool.getConnection();

			// Get the airline data
			Map<?, ?> airlines = (Map<?, ?>) SystemData.getObject("apps");

			// Get the Library DAO
			GetDocuments rdao = new GetDocuments(c);
			if (!"usrlibrary".equals(url.getLastPath())) {
				for (Iterator<?> i = airlines.values().iterator(); (entry == null) && i.hasNext();) {
					AirlineInformation aInfo = (AirlineInformation) i.next();
					if ("fleet".equals(url.getLastPath()))
						entry = rdao.getInstaller(url.getFileName(), aInfo.getDB());
					else if ("library".equals(url.getLastPath()))
						entry = rdao.getManual(url.getFileName(), aInfo.getDB());
					else if ("newsletter".equals(url.getLastPath()))
						entry = rdao.getNewsletter(url.getFileName(), aInfo.getDB());
					else if ("video".equals(url.getLastPath()))
						entry = rdao.getVideo(url.getFileName());
				}
			} else
				entry = rdao.getFile(url.getFileName());

			// Check if the file exists
			if (entry == null)
				throw new NotFoundException("Cannot find file - " + url.getFileName());
			else if (!entry.file().exists())
				throw new NotFoundException("Cannot find " + entry.file().getAbsolutePath());

			// Validate access to the file
			SecurityContext sctxt = new ServletSecurityContext(req);
			FleetEntryAccessControl access = new FleetEntryAccessControl(sctxt, entry);
			access.validate();
			if (!access.getCanView())
				throw new ForbiddenException("Cannot view " + url.getFileName());

			// Get the DAO and log the download
			if (sctxt.isAuthenticated()) {
				SetLibrary wdao = new SetLibrary(c);
				wdao.download(entry.getFileName(), sctxt.getUser().getID());
			}
		} catch (ConnectionPoolException cpe) {
			log.error("Error downloading {} - {}", url.getFileName(), cpe.getMessage());
			rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ControllerException ce) {
			String msg = "Error downloading " + url.getFileName() + " - " + ce.getMessage();
			if (ce.isWarning())
				log.warn(msg);
			else
				log.error(msg, ce.getLogStackDump() ? ce : null);

			entry = null;
			rsp.sendError(ce.getStatusCode());
		} finally {
			pool.release(c);
		}

		// Abort if we got an error
		if (entry == null)
			return;

		// Log Status message
		log.info("Downloading {}, {} bytes", url.getFileName().toLowerCase(), Long.valueOf(entry.getSize()));

		// Set the response headers
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength((int) entry.file().length());
		if ("pdf".equals(url.getExtension()))
			rsp.setContentType("application/pdf");
		else
			rsp.setContentType("application/octet-stream");
		
		// Check if we stream via mod_xsendfile
		boolean doSendFile = SystemData.getBoolean("airline.files.sendfile");
		if (doSendFile && entry.file().exists()) {
			log.info("Sending {} via mod_xsendfile", entry.getFileName());
			rsp.addHeader("X-Sendfile", entry.file().getAbsolutePath());
			return;
		}

		// Stream the file
		boolean isComplete = false;
		rsp.setBufferSize(BUFFER_SIZE);
		TaskTimer tt = new TaskTimer();
		try {
			byte[] buf = new byte[BUFFER_SIZE];
			try (InputStream is = new FileInputStream(entry.file())) {
				OutputStream out = rsp.getOutputStream();
				int bytesRead = is.read(buf, 0, BUFFER_SIZE);
				while (bytesRead != -1) {
					out.write(buf, 0, bytesRead);
					bytesRead = is.read(buf, 0, BUFFER_SIZE);
				}
				
				out.flush();
			}
			
			isComplete = true;
		} catch (IOException ie) {
			log.info("Download canceled");
		} catch (Exception e) {
			log.atError().withThrowable(e).log("Error downloading {}" + entry.getName());
		}

		// Close the file and log download time
		long totalTime = Math.max(1, tt.stop());
		if (isComplete)
			log.info("{} download complete, {}s, bytes/sec", entry.getFileName().toLowerCase(), Long.valueOf(totalTime / 1000), Long.valueOf(entry.getSize() * 1000 / totalTime));
	}
}