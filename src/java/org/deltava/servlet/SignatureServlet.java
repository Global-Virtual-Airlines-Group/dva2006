// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.*;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.*;

/**
 * The Signature Image serving Servlet. This serves Water Cooler signature images.
 * @author Luke
 * @version 7.0
 * @since 2.6
 */

public class SignatureServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(SignatureServlet.class);

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
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
		AirlineInformation ai = SystemData.getApp(url.getLastPath());
		if (ai == null)
			return -1;

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
		java.time.Instant lastMod = null; Connection c = null;
		try {
			c = jdbcPool.getConnection();

			// Get the retrieve image DAO
			GetImage dao = new GetImage(c);
			lastMod = dao.getSigModified(imgID, ai.getDB());
		} catch (ConnectionPoolException cpe) {
			log.warn("Connection pool error - " + cpe.getMessage());
		} catch (ControllerException ce) {
			if (ce.isWarning())
				log.warn("Error retrieving image data - " + ce.getMessage());
			else
				log.error("Error retrieving image data - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
		} finally {
			jdbcPool.release(c);
		}

		return (lastMod == null) ? -1 : lastMod.toEpochMilli();
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
		AirlineInformation ai = SystemData.getApp(url.getLastPath());
		if (ai == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

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
			imgBuffer = dao.getSignatureImage(imgID, ai.getDB());
		} catch (ConnectionPoolException cpe) {
			log.warn("Connection pool error - " + cpe.getMessage());
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
		rsp.setDateHeader("Expires", System.currentTimeMillis() + (365L * 86400 * 1000));

		// Dump the data to the output stream
		try (OutputStream out = rsp.getOutputStream()) {
			out.write(imgBuffer);
			rsp.flushBuffer();
		} catch (IOException ie) {
			// NOOP
		}
	}
}