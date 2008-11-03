// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.service.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.VersionInfo;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet to handle Web Service data requests.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class WebServiceServlet extends BasicAuthServlet {

	// Web services realm
	private static final String WS_REALM = "\"DVA Web Services\"";

	private static final Logger log = Logger.getLogger(WebServiceServlet.class);
	private final Map<String, WebService> _svcs = new HashMap<String, WebService>();

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Web Service Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Initializes the servlet. This loads the service map.
	 * @throws ServletException if an error occurs
	 * @see ServiceFactory#load(String)
	 */
	public void init() throws ServletException {
		log.info("Initializing");
		try {
			_svcs.putAll(ServiceFactory.load(SystemData.get("config.services")));
		} catch (IOException ie) {
			throw new ServletException(ie);
		}
	}

	/**
	 * Shuts down the servlet. This just logs a message to the servlet log.
	 */
	public void destroy() {
		log.info("Shutting Down");
	}

	/**
    * Processes HTTP GET requests for web services.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {

		// Get the web service
		URLParser parser = new URLParser(req.getRequestURI());
		WebService svc = _svcs.get(parser.getName().toLowerCase());
		if (svc == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown Service");
			return;
		}

		// Check if we need to be authenticated
		Pilot usr = (Pilot) req.getUserPrincipal();
		if (svc.isSecure() && (usr == null)) {
			usr = authenticate(req);
			if (usr == null) {
				challenge(rsp, WS_REALM);
				return;
			}
		}
		
		// Generate the service context
		ServiceContext ctx = new ServiceContext(req, rsp);
		ctx.setUser(usr);
		if (svc.isLogged())
			log.info("Executing Web Service " + svc.getClass().getName());

		// Execute the Web Service
		try {
			rsp.setStatus(svc.execute(ctx));
		} catch (ServiceException se) {
			if (se.isWarning())
				log.warn("Error executing Web Service - " + se.getMessage());
			else
				log.error("Error executing Web Service - " + se.getMessage(), se.getLogStackDump() ? se : null);	

			try {
				rsp.sendError(se.getCode(), se.getMessage());
			} catch (Exception e) {
				// empty
			}
		} finally {
			// Disable the cache
			rsp.setHeader("Cache-Control", "no-cache");
		}
	}

	/**
    * Processes HTTP POST requests for web services. This redirects to the GET handler.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    * @see WebServiceServlet#doGet(HttpServletRequest, HttpServletResponse)
    */
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		doGet(req, rsp);
	}
}