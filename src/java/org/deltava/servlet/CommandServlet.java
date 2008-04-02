// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servlet.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.jdbc.*;

import org.deltava.util.*;
import org.deltava.util.redirect.RequestStateHelper;
import org.deltava.util.system.SystemData;

/**
 * The main command controller. This is the application's brain stem.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class CommandServlet extends GenericServlet implements Thread.UncaughtExceptionHandler {

	private static final Logger log = Logger.getLogger(CommandServlet.class);

	private static final int MAX_EXEC_TIME = 20000;
	private static final String ERR_PAGE = "/jsp/error/error.jsp";

	private final Map<String, Command> _cmds = new HashMap<String, Command>();
	private Command _defaultCmd;

	protected final BlockingQueue<CommandLog> _cmdLogPool = new LinkedBlockingQueue<CommandLog>();
	private int _maxCmdLogSize;
	private CommandLogger _logThread;

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	public String getServletInfo() {
		return "Command Controller Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	private class CommandLogger extends Thread {
		
		private final Logger tlog = Logger.getLogger(CommandLogger.class);
		private int _maxSize;

		CommandLogger(int maxSize) {
			super(SystemData.get("airline.code") + " Command Logger");
			setDaemon(true);
			_maxSize = Math.max(1, maxSize);
		}

		public void run() {
			tlog.info("Started");
			while (!isInterrupted()) {
				try {
					sleep(2000);
				} catch (InterruptedException ie) {
					interrupt();
					tlog.warn("Interrupted");
				}

				// Check if we need to log
				if ((_cmdLogPool.size() >= _maxSize) || isInterrupted()) {
					Collection<CommandLog> entries = new ArrayList<CommandLog>();
					_cmdLogPool.drainTo(entries);
					ConnectionPool pool = getConnectionPool();
					Connection c = null;
					try {
						c = pool.getConnection(true);
						SetSystemData swdao = new SetSystemData(c);
						swdao.logCommands(entries);
						if (tlog.isDebugEnabled())
							tlog.debug("Wrote command statistics");
					} catch (DAOException de) {
						tlog.warn("Error writing command result staitistics - " + de.getMessage());
					} finally {
						pool.release(c);
						_cmdLogPool.clear();
					}
				}
			}
			
			tlog.info("Stopped");
		}
	}

	/**
	 * Initializes the servlet. This loads the command map.
	 * @throws ServletException if an error occurs
	 * @see CommandFactory#load(String, ServletContext)
	 */
	public void init() throws ServletException {
		log.info("Initializing");
		try {
			Map<String, Command> cmds = CommandFactory.load(SystemData.get("config.commands"), getServletContext());
			_cmds.putAll(cmds);

			// Initialize the default command
			_defaultCmd = cmds.get(getInitParameter("defaultCommand"));
			if ((_defaultCmd == null) && (!cmds.isEmpty()))
				_defaultCmd = cmds.values().iterator().next();
		} catch (IOException ie) {
			throw new ServletException(ie);
		}

		// Initialize the redirection command
		try {
			Command cmd = new RedirectCommand();
			cmd.setContext(getServletContext());
			cmd.init("$redirect", "Request Redirection");
			cmd.setRoles(Collections.singleton("*"));
			_cmds.put(cmd.getID(), cmd);
		} catch (CommandException ce) {
			throw new ServletException(ce);
		}

		// Save the max command log size
		_maxCmdLogSize = SystemData.getInt("cache.cmdlog", 20);
		_logThread = new CommandLogger(_maxCmdLogSize);
		_logThread.setUncaughtExceptionHandler(this);
		_logThread.start();
	}

	/**
	 * Shuts down the servlet. This just logs a message to the servlet log.
	 */
	public void destroy() {
		log.info("Shutting Down");
		ThreadUtils.kill(_logThread, 500);
	}

	/**
	 * A private helper method to get the command name from the URL.
	 */
	private Command getCommand(String rawURL) {
		URLParser parser = new URLParser(rawURL);
		try {
			String cmdName = parser.getName().toLowerCase();
			if (cmdName.startsWith("/"))
				cmdName = cmdName.substring(1);

			return _cmds.get(cmdName);
		} catch (Exception e) {
			return _defaultCmd;
		}
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
			if (log.isDebugEnabled())
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
			switch (result.getResult()) {
			case CommandResult.REQREDIRECT:
				if (log.isDebugEnabled())
					log.debug("Preserving servlet request state");

				RequestStateHelper.save(req, result.getURL());
				rsp.sendRedirect("$redirect.do");
				break;

			case CommandResult.REDIRECT:
				if (log.isDebugEnabled())
					log.debug("Redirecting to " + result.getURL());

				rsp.sendRedirect(result.getURL());
				break;

			case CommandResult.HTTPCODE:
				if (log.isDebugEnabled())
					log.debug("Setting HTTP status " + String.valueOf(result.getHttpCode()));

				rsp.setStatus(result.getHttpCode());
				break;

			default:
			case CommandResult.FORWARD:
				if (log.isDebugEnabled())
					log.debug("Forwarding to " + result.getURL());

				RequestDispatcher rd = req.getRequestDispatcher(result.getURL());
				rd.forward(req, rsp);
				break;
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
			String usrName = null;
			if (req.getUserPrincipal() == null)
				usrName = "Anonymous (" + req.getRemoteHost() + ")";
			else
				usrName = req.getUserPrincipal().getName();

			if (logWarning)
				log.warn(usrName + " executing " + cmd.getName() + " - " + e.getMessage());
			else
				log.error(usrName + " executing " + cmd.getName() + " - " + e.getMessage(), logStackDump ? e : null);

			try {
				// Redirect to the error page
				RequestDispatcher rd = req.getRequestDispatcher(errPage);
				req.setAttribute("servlet_error", e.getMessage());
				req.setAttribute("servlet_exception", (e.getCause() == null) ? e : e.getCause());
				rd.forward(req, rsp);
			} catch (Exception fe) {
				try {
					rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception ee) {
					log.error("Error sending error code - " + ee.getMessage());
				}
			}
		} finally {
			long execTime = System.currentTimeMillis() - startTime;
			if (execTime < MAX_EXEC_TIME) {
				if (log.isDebugEnabled())
					log.debug("Completed in " + String.valueOf(execTime) + " ms");
			} else
				log.warn(cmd.getID() + " completed in " + String.valueOf(execTime) + " ms");

			// Create the command result statistics entry
			CommandLog cmdLog = new CommandLog(cmd.getID(), ctxt.getResult());
			cmdLog.setRemoteAddr(req.getRemoteAddr());
			cmdLog.setRemoteHost(req.getRemoteHost());
			cmdLog.setPilotID(ctxt.isAuthenticated() ? ctxt.getUser().getID() : 0);
			_cmdLogPool.add(cmdLog);
		}
	}
	
	/**
	 * Logger thread uncaught exception handler.
	 */
	public void uncaughtException(Thread t, Throwable e) {
		if (t != _logThread) {
			log.error("Unknown thread - " + t.getName(), e);
			return;
		}
		
		_logThread = new CommandLogger(_maxCmdLogSize);
		_logThread.setUncaughtExceptionHandler(this);
		_logThread.start();
		log.error("Restarted " + t.getName(), e);
	}
}