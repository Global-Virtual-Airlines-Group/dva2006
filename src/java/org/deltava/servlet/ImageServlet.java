// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.schedule.Chart;
import org.deltava.beans.system.VersionInfo;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.jdbc.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * The Image serving Servlet. This serves all database-contained images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageServlet extends BasicAuthServlet {

	private static final Logger log = Logger.getLogger(ImageServlet.class);

	private static final String CHART_REALM = "\"Approach Charts\"";
	private static final String EXAM_REALM = "\"Pilot Examinations\"";

	private static final int IMG_CHART = 0;
	private static final int IMG_GALLERY = 1;
	private static final int IMG_SIG = 2;
	private static final int IMG_EXAM = 3;

	private static final String[] IMG_TYPES = { "charts", "gallery", "sig", "exam_rsrc" };

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Database Image Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * A helper method to get the image type from the URL.
	 */
	private int getImageType(URLParser up) {
		for (int x = 0; x < IMG_TYPES.length; x++) {
			if (up.containsPath(IMG_TYPES[x]))
				return x;
		}

		return -1;
	}

	/**
	 * Processes HTTP GET requests for images.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if a network I/O error occurs
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Parse the URL to figure out what kind of image we want
		URLParser url = new URLParser(req.getRequestURI());
		int imgType = getImageType(url);
		if (imgType == -1) {
			log.warn("Invalid Image type - " + url.getLastPath());
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// If we're loading a chart, make sure we are authenticated
		if ((imgType == IMG_CHART) || (imgType == IMG_EXAM)) {
			Pilot usr = (Pilot) req.getUserPrincipal();
			if (usr == null)
				usr = authenticate(req);

			// Check if we're coming from a questionnaire
			String referer = req.getHeader("Referer");
			boolean fromQ = (referer != null) && referer.contains("/questionnaire.do");

			// Don't challenge if coming from a questionnaire and unauthenticated
			if ((usr == null) && ((imgType == IMG_CHART) || !fromQ)) {
				challenge(rsp, (imgType == IMG_CHART) ? CHART_REALM : EXAM_REALM);
				return;
			}
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
		log.debug("Getting " + IMG_TYPES[imgType] + " image ID" + String.valueOf(imgID));
		Connection c = null;
		try {
			c = jdbcPool.getConnection();

			// Get the retrieve image DAO and execute the right method
			GetImage dao = new GetImage(c);
			switch (imgType) {
				case IMG_CHART:
					imgBuffer = dao.getChart(imgID);
					rsp.setHeader("Cache-Control", "private");
					rsp.setIntHeader("max-age", 300);
					break;

				case IMG_GALLERY:
					// Validate that we can view the image
					GetGallery gdao = new GetGallery(c);
					Image img = gdao.getImageData(imgID, url.getLastPath());
					if (img.getThreadID() != 0) {
						GetCoolerChannels chdao = new GetCoolerChannels(c);
						GetCoolerThreads tdao = new GetCoolerThreads(c);
						MessageThread mt = tdao.getThread(img.getThreadID(), false);
						Channel ch = chdao.get(mt.getChannel());
						
						// Validate access to the thread
						CoolerThreadAccessControl access = new CoolerThreadAccessControl(new ServletSecurityContext(req));
						access.updateContext(mt, ch);
						access.validate();
						if (!access.getCanRead())
							throw new NotFoundException("Cannot view Image - Cannot read Message Thread " + mt.getID());
					}
					
					// Serve the image
					imgBuffer = dao.getGalleryImage(imgID, url.getLastPath());
					rsp.setHeader("Cache-Control", "private");
					rsp.setIntHeader("max-age", 600);
					break;

				case IMG_SIG:
					imgBuffer = dao.getSignatureImage(imgID, url.getLastPath());
					rsp.setHeader("Cache-Control", "public");
					rsp.setIntHeader("max-age", 600);
					break;

				case IMG_EXAM:
					imgBuffer = dao.getExamResource(imgID);
					rsp.setHeader("Cache-Control", "private");
					rsp.setIntHeader("max-age", 60);
					break;

				default:
					log.warn("Unknown image type - " + req.getRequestURI());
			}
		} catch (ControllerException ce) {
			if (ce.isWarning()) {
				log.warn("Error retrieving image - " + ce.getMessage());
			} else {
				log.error("Error retrieving image - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
			}
		} finally {
			jdbcPool.release(c);
		}

		// If we got nothing, then throw an error
		if (imgBuffer == null) {
			log.error("Cannot find image " + url.getLastPath() + "/" + imgID);
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Check for PDF
		boolean isPDF = true;
		for (int x = 0; x < Chart.PDF_MAGIC.length(); x++)
			isPDF &= (imgBuffer[x] == Chart.PDF_MAGIC.getBytes()[x]);

		// Get the image type
		if (!isPDF) {
			ImageInfo info = new ImageInfo(imgBuffer);
			if (!info.check()) {
				rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
			
			rsp.setContentType(info.getMimeType());
		} else
			rsp.setContentType("application/pdf");

		// Set the content-type and content length
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength(imgBuffer.length);
		rsp.setBufferSize((imgBuffer.length > 65520) ? 65520 : imgBuffer.length);

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