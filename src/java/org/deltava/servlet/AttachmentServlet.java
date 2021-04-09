// Copyright 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.Connection;
import java.time.Instant;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.ACARSError;
import org.deltava.beans.event.Briefing;
import org.deltava.beans.event.Event;
import org.deltava.beans.stats.Tour;
import org.deltava.beans.system.*;

import org.deltava.dao.*;

import org.deltava.security.SecurityContext;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.*;

/**
 * A servlet to download file attachments.
 * @author Luke
 * @version 10.0
 * @since 7.3
 */

public class AttachmentServlet extends DownloadServlet {
	
	private static final Logger log = Logger.getLogger(AttachmentServlet.class);
	
	private enum AttachType implements FileType {
		ISSUE("issue", "Technology Issues"), ERROR("error_log", "ACARS Error Logs"), EVENT("ebrief", "Online Event Briefings"), HELPDESK("helpdesk", "Help Desk Issues"), TOUR("tbrief", "Flight Tour Briefings");
		
		private final String _urlPart;
		private final String _realm;
		
		AttachType(String urlPart, String realm) {
			_urlPart = urlPart;
			_realm = realm;
		}
		
		@Override
		public String getURLPart() {
			return _urlPart;
		}
		
		public String getRealm() {
			return _realm;
		}
	}
	
	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "File Attachment Servlet " + VersionInfo.TXT_COPYRIGHT;
	}
	
	/**
	 * Processes HTTP GET requests for attachments.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if a network I/O error occurs
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Parse the URL to figure out what kind of image we want
		URLParser url = new URLParser(req.getRequestURI());
		AttachType fType = (AttachType) getFileType(url, AttachType.values());
		if (fType == null) {
			log.warn("Invalid File type - " + url.getLastPath());
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// Make sure we are authenticated
		Pilot usr = (Pilot) req.getUserPrincipal();
		if (usr == null)
			usr = authenticate(req);
		if (usr == null) {
			challenge(rsp, fType.getRealm());
			return;
		}

		// Get the database ID
		int dbID = 0;
		try {
			String name = url.getName();
			if (name.indexOf('.') != -1)
				name = name.substring(0, name.indexOf('.'));

			dbID = StringUtils.parseHex(name);
		} catch (Exception e) {
			log.warn("Error parsing ID " + url.getName() + " - " + e.getClass().getName());
			rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		// Get the connection pool
		ConnectionPool jdbcPool = getConnectionPool();

		byte[] buffer = null;
		if (log.isDebugEnabled())
			log.debug("Getting " + fType.name() + " attachment ID" + dbID);
		Connection c = null;
		try {
			c = jdbcPool.getConnection();
			
			SecurityContext sctx = new ServletSecurityContext(req);
			switch (fType) {
			case ISSUE:
				// Validate that we can view the issue
				GetIssue idao = new GetIssue(c);
				Issue i = idao.get(StringUtils.parse(url.getLastPath(), -1));
				if (i == null)
					throw new NotFoundException("Invalid Issue - " + url.getLastPath());
				
				// Validate access to the thread
				IssueAccessControl access = new IssueAccessControl(sctx, i);
				access.validate();
				if (!access.getCanRead())
					throw new ForbiddenException("Cannot view Image - Cannot read Issue " + i.getID());
				
				// Serve the file
				IssueComment iFile = idao.getFile(dbID);
				if (iFile == null)
					throw new NotFoundException("Cannot find image " + url.getLastPath() + "/" + dbID);
				
				buffer = iFile.getBuffer();
				rsp.setContentType("application/octet-stream");
				rsp.setHeader("Content-disposition", String.format("attachment; filename=%s", iFile.getName()));
				break;
				
			case ERROR:
				// Get the error report
				GetACARSErrors errdao = new GetACARSErrors(c);
				ACARSError err = errdao.get(dbID);
				if (err == null)
					throw new NotFoundException("Invalid Error Report - " + dbID);
				else if (!err.isLoaded())
					throw new NotFoundException("No Log attached to Error Report - " + dbID);
				
				// Check access
				ErrorLogAccessControl eac = new ErrorLogAccessControl(sctx, err);
				eac.validate();
				if (!eac.getCanRead())
					throw new ForbiddenException("Cannot view Errort Report");
				
				buffer = err.getLogData();
				rsp.setContentType("text/plain");
				rsp.setHeader("Content-disposition", String.format("attachment; filename=acars_error_%d.log", Integer.valueOf(dbID)));
				rsp.setIntHeader("max-age", 3600);
				break;
				
			case HELPDESK:
				GetHelp hdao = new GetHelp(c);
				org.deltava.beans.help.Issue hi = hdao.getIssue(dbID);
				if (hi == null)
					throw new NotFoundException("Invalid Help Desk Issue - " + dbID);
				
				// Validate access to the thread
				try {
					HelpDeskAccessControl ac = new HelpDeskAccessControl(sctx, hi);
					ac.validate();
				} catch (AccessControlException ace) {
					throw new ForbiddenException("Cannot view Image - Cannot read Issue " + hi.getID());
				}
				
				Instant dt = Instant.ofEpochMilli(Long.parseLong(url.getLastPath()));
				org.deltava.beans.help.IssueComment cmt = hdao.getFile(hi.getID(), dt);
				if (cmt == null)
					throw new NotFoundException("Cannot find image " + url.getLastPath() + "/" + dbID);
				
				buffer = cmt.getBuffer();
				rsp.setContentType("application/octet-stream");
				rsp.setHeader("Content-disposition", "attachment; filename=" + cmt.getName());
				break;
				
			case EVENT:
				GetEvent edao = new GetEvent(c);
				Event e = edao.get(dbID);
				if ((e == null) || (e.getBriefing() == null))
					throw new NotFoundException("Invalid Event - " + url.getLastPath());
				
				// Get the briefing
				Briefing eb = e.getBriefing();
				buffer = eb.getBuffer();
				rsp.setIntHeader("max-age", 3600);
				rsp.setContentType(eb.getContentType());
				rsp.setHeader("Content-disposition", String.format("attachment; filename=eventBriefing_%d.%s", Integer.valueOf(dbID), eb.getExtension()));
				break;
				
			case TOUR:
				GetTour tdao = new GetTour(c);
				Tour t = tdao.get(dbID, SystemData.get("airline.db"));
				if ((t == null) || (t.getSize() < 1))
					throw new NotFoundException("Invalid Flight Tour - " + url.getLastPath());
				
				// Get the briefing
				buffer = t.getBuffer();
				rsp.setIntHeader("max-age", 3600);
				rsp.setContentType(t.getContentType());
				rsp.setHeader("Content-disposition", String.format("attachment; filename=tourBriefing_%d.%s", Integer.valueOf(dbID), t.getExtension()));
				break;
				
			default:
				throw new NotFoundException("Unknown file type - " + req.getRequestURI());
			}
		} catch (ConnectionPoolException cpe) {
			log.error(cpe.getMessage());
		} catch (ControllerException ce) {
			if (ce.isWarning())
				log.warn("Error retrieving image - " + ce.getMessage());
			else
				log.error("Error retrieving image - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
			
			rsp.sendError(ce.getStatusCode());
		} finally {
			jdbcPool.release(c);
		}

		// If we got nothing, abort
		if (buffer == null)
			return;

		// Set the content-type and content length
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength(buffer.length);
		rsp.setBufferSize(Math.min(65536, buffer.length + 16));
		rsp.setHeader("Cache-Control", "private");

		// Dump the data to the output stream
		try (OutputStream os = rsp.getOutputStream()) {
			os.write(buffer);
			rsp.flushBuffer();
		} catch (Exception e) {
			// NOOP
		}
	}
}