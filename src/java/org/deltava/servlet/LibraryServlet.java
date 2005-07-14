package org.deltava.servlet;

import java.io.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.fleet.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.jdbc.ConnectionPool;

import org.deltava.dao.GetLibrary;
import org.deltava.dao.SetLibrary;
import org.deltava.dao.DAOException;

import org.deltava.security.SecurityContext;
import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.URLParser;
import org.deltava.util.system.SystemData;

/**
 * A servlet to serve Fleet/Document Library files.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LibraryServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(LibraryServlet.class);

	private static final int BUFFER_SIZE = 102400;

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Fleet Library Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Private helper method to get a File handle to the requested resource.
	 */
	private File getResource(URLParser url) {
		return new File(SystemData.get("path.library"), url.getFileName());
	}

	/**
	 * Determines the last date a resource was modified for simple cacheing.
	 * @param req the HTTP servlet request
	 * @return a 32-bit UNIX timestamp, or -1 if unknown
	 */
	public long getLastModified(HttpServletRequest req) {

		// Get the resource we want
		URLParser url = new URLParser(req.getRequestURI());
		File f = getResource(url);
		if (!f.exists())
			return -1;

		// Return the file date/time
		return f.lastModified();
	}

	/**
	 * Processes HTTP GET requests for Fleet Library resources.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Get the resource we want
		URLParser url = new URLParser(req.getRequestURI());
		File f = getResource(url);
		if (!f.exists()) {
			log.warn("Cannot find " + f.getAbsolutePath());
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Log Status message
		log.info("Downloading " + f.getName().toLowerCase() + ", " + f.length() + " bytes");

		// Get the connection pool
		ConnectionPool jdbcPool = getConnectionPool();
		Connection c = null;
		try {
			c = jdbcPool.getConnection();

			// Get the Library DAO
			GetLibrary rdao = new GetLibrary(c);
			FleetEntry entry = "fleet".equals(url.getLastPath()) ? (FleetEntry) rdao.getInstaller(f.getName()) : (FleetEntry) rdao
					.getManual(f.getName());

			// Validate access to the file
			SecurityContext sctxt = new ServletSecurityContext(req);
			FleetEntryAccessControl access = new FleetEntryAccessControl(sctxt, entry);
			try {
				access.validate();
			} catch (Exception e) {
				log.warn("Unauthorized access to " + f.getName());
				jdbcPool.release(c);

				// Set error code and return
				rsp.sendError(403);
				return;
			}

			// Get the DAO and log the download
			if (sctxt.isAuthenticated()) {
				SetLibrary wdao = new SetLibrary(c);
				wdao.download(f.getName(), sctxt.getUser().getID());
			}
		} catch (DAOException de) {
			log.error("Error logging download - " + de.getMessage());
		} finally {
			jdbcPool.release(c);
		}

		// Set the response headers
		rsp.setBufferSize(BUFFER_SIZE);
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength((int) f.length());
		if ("pdf".equals(url.getExtension())) {
			rsp.setContentType("application/pdf");
		} else {
			rsp.setContentType("application/octet-stream");
		}

		// Stream the file
		long startTime = System.currentTimeMillis();
		try {
			byte[] buf = new byte[BUFFER_SIZE];
			InputStream is = new FileInputStream(f);
			OutputStream out = rsp.getOutputStream();
			int bytesRead = is.read(buf, 0, BUFFER_SIZE);
			while (bytesRead != -1) {
				out.write(buf, 0, bytesRead);
				bytesRead = is.read(buf, 0, BUFFER_SIZE);
			}

			is.close();
			out.flush();
		} catch (IOException ie) {
			log("Error streaming " + f.getAbsolutePath() + " - " + ie.getMessage());
			throw ie;
		}

		// Close the file and log download time
		long totalTime = System.currentTimeMillis() - startTime;
		log.info(f.getName().toLowerCase() + " download complete, " + (totalTime / 1000) + "s, " + (f.length() * 1000 / totalTime)
				+ " bytes/sec");
	}
}