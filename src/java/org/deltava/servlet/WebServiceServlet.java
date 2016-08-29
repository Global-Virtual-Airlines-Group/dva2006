// Copyright 2005, 2007, 2008, 2011, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

import org.apache.log4j.Logger;

import com.newrelic.api.agent.NewRelic;

import org.deltava.service.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.VersionInfo;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet to handle Web Service data requests.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

@MultipartConfig
public class WebServiceServlet extends BasicAuthServlet {

	// Web services realm
	private static final String WS_REALM = "\"%A Web Services\"";

	private static final Logger log = Logger.getLogger(WebServiceServlet.class);
	private final Map<String, WebService> _svcs = new HashMap<String, WebService>();

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "Web Service Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Initializes the servlet. This loads the service map.
	 * @throws ServletException if an error occurs
	 * @see ServiceFactory#load(String)
	 */
	@Override
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
	@Override
	public void destroy() {
		log.info("Shutting Down");
	}

	/**
    * Processes HTTP GET requests for web services.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {

		// Get the web service
		URLParser parser = new URLParser(req.getRequestURI());
		if (parser.size() > 1) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown Service - " + req.getRequestURI());
			return;
		}
			
		WebService svc = _svcs.get(parser.getName().toLowerCase());
		if (svc == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown Service");
			return;
		}
		
		// Check if we need SSL
		NewRelic.setTransactionName("Web Service", svc.getClass().getSimpleName());
		if (!req.isSecure() && svc.requiresSSL()) {
			log.info("Redirecting " + req.getRequestURI() + " to secure connection");
			rsp.sendRedirect("https://" + req.getServerName() + req.getRequestURI());
			return;
		}

		// Check if we need to be authenticated
		Pilot usr = (Pilot) req.getUserPrincipal();
		if (svc.isSecure() && (usr == null)) {
			usr = authenticate(req);
			if (usr == null) {
				challenge(rsp, WS_REALM.replace("%A", SystemData.get("airline.name")));
				return;
			}
		} else if (usr != null)
			NewRelic.setUserName(usr.getName());
		
		// Generate the service context
		ServiceContext ctx = new ServiceContext(req, rsp);
		ctx.setUser(usr);
		if (svc.isLogged())
			log.info("Executing Web Service " + svc.getClass().getName());

		// Execute the Web Service
		TaskTimer tt = new TaskTimer();
		try {
			rsp.setStatus(svc.execute(ctx));
		} catch (Exception e) {
			int resultCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			if (e instanceof ServiceException) {
				ServiceException se = (ServiceException) e;
				resultCode = se.getCode();
				if (se.isWarning())
					log.warn("Error executing Web Service - " + e.getMessage());
				else
					log.error("Error executing Web Service - " + e.getMessage(), se.getLogStackDump() ? e : null);	
			} else
				log.error("Error executing " + parser.getName() + " - " + e.getMessage(), e);

			try {
				rsp.sendError(resultCode, e.getMessage());
			} catch (Exception e2) {
				// empty
			}
		} finally {
			tt.stop();
		}
		
		// Log excessive execution
		NewRelic.recordResponseTimeMetric(svc.getClass().getSimpleName(), tt.getMillis());
		if (tt.getMillis() > 5000)
			log.warn("Excessive execution time for " + parser.getName().toLowerCase() + " - " + tt.getMillis() + "ms");
	}

	/**
    * Processes HTTP POST requests for web services. This redirects to the GET handler.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    * @see WebServiceServlet#doGet(HttpServletRequest, HttpServletResponse)
    */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		doGet(req, rsp);
	}
}