// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.sql.Connection;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servlet.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.commands.*;
import org.deltava.jdbc.*;

import org.deltava.dao.SetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.URLParser;
import org.deltava.util.RoleUtils;

import org.deltava.util.redirect.RequestStateHelper;
import org.deltava.util.system.SystemData;

/**
 * The main command controller. This is the application's brain stem.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(CommandServlet.class);
	
	private static final int MAX_EXEC_TIME = 20000;
	private static final String ERR_PAGE = "/jsp/error/error.jsp";

	private final Map<String, Command> _cmds = new HashMap<String, Command>();
	
	private final Collection<CommandLog> _cmdLogPool = new ArrayList<CommandLog>();
	private int _maxCmdLogSize;

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Command Controller Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Initializes the servlet. This loads the command map.
	 * @throws ServletException if an error occurs
	 * @see CommandFactory#load(String, ServletContext)
	 */
	public void init() throws ServletException {
		log.info("Initializing");
		try {
			_cmds.putAll(CommandFactory.load(SystemData.get("config.commands"), getServletContext()));
		} catch (IOException ie) {
			throw new ServletException(ie);
		}

		// Initialize the redirection command
		Command cmd = new RedirectCommand();
		try {
			cmd.setContext(getServletContext());
			cmd.init("$redirect", "Request Redirection");
			cmd.setRoles(Arrays.asList(new String[] { "*" }));
			_cmds.put(cmd.getID(), cmd);
		} catch (CommandException ce) {
			throw new ServletException(ce);
		}

		// Save the max command log size
		_maxCmdLogSize = SystemData.getInt("cache.cmdlog", 20);
	}

	/**
	 * Shuts down the servlet. This just logs a message to the servlet log.
	 */
	public void destroy() {
		log.info("Shutting Down");
	}

	/**
	 * A private helper method to get the command name from the URL.
	 */
	private Command getCommand(String rawURL) {
		URLParser parser = new URLParser(rawURL);
		String cmdName = parser.getName().toLowerCase();
		return _cmds.get(cmdName);
	}
	
	/**
	 * A private helper method to dump the command logs.
	 */
	private void dumpCommandLogs() {
		ConnectionPool pool = getConnectionPool();
		Connection c = null;
		try {
			c = pool.getConnection(true);
			SetSystemData swdao = new SetSystemData(c);
			swdao.logCommands(_cmdLogPool);
		} catch (DAOException de) {
			log.warn("Error writing command result staitistics - " + de.getMessage());
		} finally {
			pool.release(c);
			_cmdLogPool.clear();
		}

		log.debug("Batched command logs");
	}

	/**
	 * POST request handler for this servlet. POST and GET requests are handled the same way.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @see CommandServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
		doGet(req, rsp);
	}

	/**
	 * GET request handler for this servlet.
	 * @param req the servlet request
	 * @param rsp the servlet response
	 * @throws IOException if a network I/O error occurs
	 * @throws ServletException if the error handler cannot forward to the error page
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
		long startTime = System.currentTimeMillis();

		// Get the command
		Command cmd = getCommand(req.getRequestURI());
		if (cmd == null) {
			RequestDispatcher rd = req.getRequestDispatcher(ERR_PAGE);
			req.setAttribute("servlet_error", "Command not found");
			log.warn("Command not found - " + req.getRequestURI());
			rd.forward(req, rsp);
			return;
		}

		// Create the command context
		CommandContext ctxt = new CommandContext(req, rsp);
		try {
			// Validate command access
			if (!RoleUtils.hasAccess(ctxt.getRoles(), cmd.getRoles())) {
				ControllerException ce = new CommandException("Not Authorized to execute", false);
				ce.setForwardURL("/jsp/error/securityViolation.jsp");
				ce.setWarning(true);
				throw ce;
			}

			// If we are not executing the redirection command, clear the redirection state data in the session
			if (!(cmd instanceof RedirectCommand))
				RequestStateHelper.clear(req);

			// Execute the command
			log.debug("Executing " + req.getMethod() + " " + cmd.getName());
			cmd.execute(ctxt);
			ctxt.setCacheHeaders();
			CommandResult result = ctxt.getResult();
			result.complete();
			
			// Check for empty result
			if (result.getURL() == null) {
				ControllerException ce = new CommandException("Null result URL from " + req.getRequestURI(), false);
				ce.setForwardURL("/home.do");
				ce.setWarning(true);
				throw ce;
			}

			// Redirect/forward/send status code
			try {
				switch (result.getResult()) {
					case CommandResult.REQREDIRECT:
						log.debug("Preserving servlet request state");
						RequestStateHelper.save(req, result.getURL());
						result.setURL("$redirect.do");

					case CommandResult.REDIRECT:
						log.debug("Redirecting to " + result.getURL());
						rsp.sendRedirect(result.getURL());
						break;

					case CommandResult.HTTPCODE:
						log.debug("Setting HTTP status " + String.valueOf(result.getHttpCode()));
						rsp.setStatus(result.getHttpCode());
						break;

					default:
					case CommandResult.FORWARD:
						log.debug("Forwarding to " + result.getURL());
						RequestDispatcher rd = req.getRequestDispatcher(result.getURL());
						rd.forward(req, rsp);
						break;
				}
			} catch (Exception e) {
				throw new CommandException("Error forwarding to " + result.getURL(), e);
			}
		} catch (Exception e) {
			String errPage = ERR_PAGE;
			boolean logWarning = false;
			boolean logStackDump = true;
			if (e instanceof CommandException) {
				CommandException ce = (CommandException) e;
				if (ce.getForwardURL() != null) 
					errPage = ce.getForwardURL();
				
				logWarning = ce.isWarning();
				logStackDump = ce.getLogStackDump();
			}
			
			// Log the error
			String usrName = (req.getUserPrincipal() == null) ? "Anonymous" : req.getUserPrincipal().getName();
			if (logWarning) {
				log.warn(usrName + " executing " + cmd.getName() + " - " + e.getMessage());
			} else {
				log.error(usrName + " executing " + cmd.getName() + " - " + e.getMessage(),
						logStackDump ? e : null);
			}

			// Redirect to the error page
			RequestDispatcher rd = req.getRequestDispatcher(errPage);
			req.setAttribute("servlet_error", e.getMessage());
			req.setAttribute("servlet_exception", (e.getCause() == null) ? e : e.getCause());
			try {
				rd.forward(req, rsp);
			} catch (Exception fe) {
				rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} finally {
			long execTime = System.currentTimeMillis() - startTime;
			if (execTime < MAX_EXEC_TIME) {
				log.debug("Completed in " + String.valueOf(execTime) + " ms");
			} else {
				log.warn(cmd.getID() + " completed in " + String.valueOf(execTime) + " ms");
			}

			// Create the command result statistics entry
			CommandLog cmdLog = new CommandLog((cmd == null) ? "null" : cmd.getID(), ctxt.getResult());
			cmdLog.setRemoteAddr(req.getRemoteAddr());
			cmdLog.setRemoteHost(req.getRemoteHost());
			cmdLog.setPilotID(ctxt.isAuthenticated() ? ctxt.getUser().getID() : 0);

			// Add to the log pool
			synchronized (_cmdLogPool) {
				_cmdLogPool.add(cmdLog);

				// If the pool is full, batch the entries
				if (_cmdLogPool.size() >= _maxCmdLogSize)
					dumpCommandLogs();
			}
		}
	}
}