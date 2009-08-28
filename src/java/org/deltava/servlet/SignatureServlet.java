// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.VersionInfo;

import org.deltava.jdbc.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * The Signature Image serving Servlet. This serves Water Cooler signature images.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class SignatureServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(SignatureServlet.class);

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Signature Image Servlet " + VersionInfo.TXT_COPYRIGHT;
	}
	
	/**
	 * Returns the last modification date of a signature image.
	 * @param req the HTTP request
	 * @return the last modification date/time as a Unix timestamp, or -1 if unknown
	 */
	@Override
	public long getLastModified(HttpServletRequest req) {
		
		// Parse the URL to figure out what kind of image we want
		URLParser url = new URLParser(req.getRequestURI());

		// Get the image ID
		int imgID = 0;
		try {
			String name = url.getName();
			if (name.indexOf('.') != -1)
				name = name.substring(0, name.indexOf('.'));

			imgID = StringUtils.parseHex(name);
		} catch (Exception e) {
			log.warn("Error parsing ID " + url.getName() + " - " + e.getClass().getName());
			return -1;
		}
		
		// Get the connection pool
		ConnectionPool jdbcPool = getConnectionPool();
		java.util.Date lastMod = null; Connection c = null;
		try {
			c = jdbcPool.getConnection();

			// Get the retrieve image DAO
			GetImage dao = new GetImage(c);
			lastMod = dao.getSigModified(imgID, url.getLastPath());
		} catch (ControllerException ce) {
			if (ce.isWarning())
				log.warn("Error retrieving image data - " + ce.getMessage());
			else
				log.error("Error retrieving image data - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
		} finally {
			jdbcPool.release(c);
		}

		// Return the date/time
		return (lastMod == null) ? -1 : lastMod.getTime();
	}

	/**
	 * Processes HTTP GET requests for images.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if a network I/O error occurs
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Parse the URL to figure out what kind of image we want
		URLParser url = new URLParser(req.getRequestURI());

		// Get the image ID
		int imgID = 0;
		try {
			String name = url.getName();
			if (name.indexOf('.') != -1)
				name = name.substring(0, name.indexOf('.'));

			imgID = StringUtils.parseHex(name);
		} catch (Exception e) {
			log.warn("Error parsing ID " + url.getName() + " - " + e.getClass().getName());
			rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// Get the connection pool
		ConnectionPool jdbcPool = getConnectionPool();

		byte[] imgBuffer = null;
		if (log.isDebugEnabled())
			log.debug("Getting signature image ID" + String.valueOf(imgID));

		Connection c = null;
		try {
			c = jdbcPool.getConnection();

			// Get the retrieve image DAO
			GetImage dao = new GetImage(c);
			imgBuffer = dao.getSignatureImage(imgID, url.getLastPath());
		} catch (ControllerException ce) {
			if (ce.isWarning())
				log.warn("Error retrieving image - " + ce.getMessage());
			else
				log.error("Error retrieving image - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
		} finally {
			jdbcPool.release(c);
		}

		// If we got nothing, then throw an error
		if (imgBuffer == null) {
			log.error("Cannot find image " + url.getLastPath() + "/" + imgID);
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Get the image type
		ImageInfo info = new ImageInfo(imgBuffer);
		if (!info.check()) {
			rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
			
		// Set the content-type and content length
		rsp.setContentType(info.getMimeType());
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength(imgBuffer.length);
		rsp.setBufferSize(Math.min(65536, imgBuffer.length));
		rsp.setIntHeader("max-age", 3600);

		// Dump the data to the output stream
		try {
			OutputStream out = rsp.getOutputStream();
			out.write(imgBuffer);
			rsp.flushBuffer();
			out.close();
		} catch (IOException ie) {
			// NOOP
		}
	}
}