// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class WebServiceServlet extends BasicAuthServlet {

	// Web services realm
	private static final String WS_REALM = "\"DVA Web Services\"";

	private static final Logger log = Logger.getLogger(WebServiceServlet.class);
	private Map _svcs;

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

		// Load the services
		try {
			_svcs = ServiceFactory.load(SystemData.get("config.services"));
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
	 * A private helper method to get the service from the URL.
	 */
	private WebService getService(String rawURL) throws ServletException {
		URLParser parser = new URLParser(rawURL);
		String svcName = parser.getName().toLowerCase();

		// Get the service class
		String svcClass = (String) _svcs.get(svcName);
		if (svcClass == null)
			return null;

		// Instantiate the service class
		try {
			Class svcC = Class.forName(svcClass);
			return (WebService) svcC.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new ServletException("Web Service " + svcClass + " not found");
		} catch (Exception e) {
			throw new ServletException("Error instantiating Web Service " + svcClass);
		}
	}

	/**
    * Processes HTTP GET requests for web services.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {

		// Get the web service
		WebService svc = getService(req.getRequestURI());
		if (svc == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown Service");
			return;
		}

		// Get our credentials if we're already logged in
		Pilot usr = (Pilot) req.getUserPrincipal();
		if (usr == null)
			usr = authenticate(req);

		// Check if we need to be authenticated
		if (svc.isSecure() && (usr == null)) {
			challenge(rsp, WS_REALM);
			return;
		}
		
		// Generate the service context
		ServiceContext ctx = new ServiceContext(req, rsp, getServletContext());
		ctx.setUser(usr);

		try {

			// Execute the Web Service
			if (svc.isLogged())
				log.info("Executing Web Service " + svc.getClass().getName());

			rsp.setStatus(svc.execute(ctx));
		} catch (ServiceException se) {
			if (se.isWarning()) {
				log.warn("Error executing Web Service - " + se.getMessage());
			} else {
				log.error("Error executing Web Service - " + se.getMessage(), se.getLogStackDump() ? se : null);	
			}

			try {
				rsp.sendError(se.getCode(), se.getMessage());
			} catch (Exception e) {
				// empty
			}
		}
		
		// Disable the cache
		rsp.setHeader("Cache-Control", "no-cache");
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