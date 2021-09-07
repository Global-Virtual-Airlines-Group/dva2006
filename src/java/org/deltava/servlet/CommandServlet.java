// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

import org.apache.log4j.*;

import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.taglib.ContentHelper;

import org.deltava.util.*;
import org.deltava.util.redirect.RequestStateHelper;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.*;

import com.newrelic.api.agent.NewRelic;

/**
 * The main command controller. This is the application's brain stem.
 * @author Luke
 * @version 10.1
 * @since 1.0
 */

@MultipartConfig
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
	@Override
	public String getServletInfo() {
		return "Command Controller Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	private class CommandLogger extends Thread {
		
		private final Logger tlog = Logger.getLogger(CommandLogger.class);
		private final int _maxSize;

		CommandLogger(int maxSize) {
			super(SystemData.get("airline.code") + " Command Logger");
			setDaemon(true);
			_maxSize = Math.max(1, maxSize);
		}

		@Override
		public void run() {
			tlog.info("Started");
			while (!isInterrupted()) {
				try {
					sleep(10000);
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
						c = pool.getConnection();
						SetSystemData swdao = new SetSystemData(c);
						swdao.logCommands(entries);
						if (tlog.isDebugEnabled())
							tlog.debug("Wrote command statistics");

						swdao.logAPIRequests(APILogger.drain());
					} catch (ConnectionPoolException | DAOException de) {
						tlog.warn(String.format("Error writing command result staitistics - %s", de.getMessage()));
					} finally {
						pool.release(c);
					}
				}
			}
			
			tlog.info("Stopped");
		}
	}

	/**
	 * Initializes the servlet. This loads the command map.
	 * @throws ServletException if an error occurs
	 * @see CommandFactory#load(String)
	 */
	@Override
	public void init() throws ServletException {
		log.info("Initializing");
		try {
			Map<String, Command> cmds = CommandFactory.load(SystemData.get("config.commands"));
			_cmds.putAll(cmds);

			// Initialize the default command
			_defaultCmd = cmds.get(getInitParameter("defaultCommand"));
			if ((_defaultCmd == null) && (!cmds.isEmpty()))
				_defaultCmd = cmds.values().iterator().next();
		} catch (IOException ie) {
			throw new ServletException(ie);
		}

		// Initialize the redirection command
		Command cmd = new RedirectCommand();
		cmd.init("$redirect", "Request Redirection");
		cmd.setRoles(Collections.singleton("*"));
		_cmds.put(cmd.getID(), cmd);

		// Save the max command log size
		_maxCmdLogSize = SystemData.getInt("cache.cmdlog", 20);
		_logThread = new CommandLogger(_maxCmdLogSize);
		_logThread.setUncaughtExceptionHandler(this);
		_logThread.start();
	}

	/**
	 * Shuts down the servlet. This just logs a message to the servlet log.
	 */
	@Override
	public void destroy() {
		log.info("Shutting Down");
		Thread t = _logThread; _logThread = null;
		ThreadUtils.kill(t, 500);
	}

	/*
	 * A private helper method to get the command name from the URL.
	 */
	private Command getCommand(String rawURL) {
		URLParser parser = new URLParser(rawURL);
		if (parser.size() > 1)
			return null;
		
		try {
			String cmdName = parser.getName().toLowerCase();
			if (cmdName.charAt(0) == '/')
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
	@Override
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
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
		TaskTimer tt = new TaskTimer();
		
		// Check for spiders
		HTTPContextData httpctx = (HTTPContextData) req.getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
		boolean isSpider = ((httpctx != null) && (httpctx.getBrowserType() == BrowserType.SPIDER));

		// Get the command
		Command cmd = getCommand(req.getRequestURI());
		if (cmd == null) {
			String referer = req.getHeader("Referer");
			if (!StringUtils.isEmpty(referer))
				referer = " - " + referer;
			else
				referer = "";
			
			RequestDispatcher rd = req.getRequestDispatcher(ERR_PAGE);
			req.setAttribute("servlet_error", "Command not found");
			log.warn("Command not found - " + req.getRequestURI() + referer);
			rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			rd.forward(req, rsp);
			return;
		}
		
		// Check for invalid request
		if (req.getAttribute(CommandContext.INVALIDREQ_ATTR_NAME) != null) {
			RequestDispatcher rd = req.getRequestDispatcher(ERR_PAGE);
			req.setAttribute("servlet_error", "HTTP Upload Timed Out");
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rd.forward(req, rsp);
			return;
		}

		// Create the command context
		CommandContext ctxt = new CommandContext(req, rsp);
		NewRelic.setTransactionName("Command", cmd.getName());
		if (req.getUserPrincipal() != null)
			NewRelic.setUserName(req.getUserPrincipal().getName());
		try {
			// Validate command access
			if (!RoleUtils.hasAccess(ctxt.getRoles(), cmd.getRoles())) {
				ControllerException ce = new CommandException("Not Authorized to execute", false);
				ce.setForwardURL("/jsp/error/securityViolation.jsp");
				ce.setWarning(true);
				ce.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
				throw ce;
			}

			// If we are not executing the redirection command, clear the redirection state data in the session
			if (!(cmd instanceof RedirectCommand))
				RequestStateHelper.clear(req);

			// Execute the command
			if (log.isDebugEnabled())
				log.debug(String.format("Executing %s %s", req.getMethod(), cmd.getName()));

			cmd.execute(ctxt);
			CommandResult result = ctxt.getResult();
			result.complete();

			// Check for empty result
			if (result.getURL() == null) {
				ControllerException ce = new CommandException(String.format("Null result URL from %s", req.getRequestURI()), false);
				ce.setForwardURL("/home.do");
				ce.setStatusCode(HttpServletResponse.SC_OK);
				ce.setWarning(true);
				throw ce;
			}

			// Redirect/forward/send status code
			switch (result.getType()) {
				case REQREDIRECT:
					if (log.isDebugEnabled()) log.debug("Preserving servlet request state");
					RequestStateHelper.save(req, result.getURL());
					rsp.sendRedirect("$redirect.do");
					break;
					
				case REDIRECT:
					if (log.isDebugEnabled()) log.debug(String.format("Redirecting to %s", result.getURL()));
					rsp.sendRedirect(result.getURL());
					break;
					
				case HTTPCODE:
					if (log.isDebugEnabled()) log.debug(String.format("Setting HTTP status %d", Integer.valueOf(result.getHttpCode())));
					rsp.setStatus(result.getHttpCode());
					break;
					
				default:
					if (log.isDebugEnabled()) log.debug(String.format("Forwarding to %s", result.getURL()));
					RequestDispatcher rd = req.getRequestDispatcher(result.getURL());
					rd.forward(req, rsp);
			}
		} catch (Exception e) {
			String errPage = ERR_PAGE;
			Level logLevel = Level.ERROR; boolean logStackDump = true;
			if (e instanceof CommandException) {
				CommandException ce = (CommandException) e;
				rsp.setStatus(ce.getStatusCode());
				if (ce.getForwardURL() != null)
					errPage = ce.getForwardURL();

				logStackDump = ce.getLogStackDump();
				if (ce.isWarning())
					logLevel = Level.WARN;
			}
			
			// Don't log bot notfound/SecurityErrors
			if (!logStackDump && isSpider) logLevel = Level.INFO;
			req.setAttribute("logStackDump", Boolean.valueOf(logStackDump));

			// Log the error
			String usrName = null;
			if (req.getUserPrincipal() == null)
				usrName = String.format("%s (%s)", isSpider ? "Spider" : "Anonymous", req.getRemoteHost());
			else
				usrName = req.getUserPrincipal().getName();
			
			StringBuilder urlBuf = new StringBuilder(req.getRequestURI());
			if (!StringUtils.isEmpty(req.getQueryString()))
				urlBuf.append('?').append(req.getQueryString());

			log.log(logLevel, String.format("Error on %s", urlBuf.toString()));
			log.log(logLevel, String.format("%s executing %s - %s", usrName, cmd.getName(), e.getMessage()), logStackDump ? e : null);

			// Redirect to the error page
			try {
				RequestDispatcher rd = req.getRequestDispatcher(errPage);
				ContentHelper.clearContent(req);
				req.setAttribute("servlet_error", e.getMessage());
				req.setAttribute("servlet_exception", (e.getCause() == null) ? e : e.getCause());
				rd.forward(req, rsp);
			} catch (Exception fe) {
				log.error(String.format("Error forwarding - %s", fe.getMessage()), fe);
				try {
					rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception ee) {
					log.error(String.format("Error sending error code - %s", ee.getMessage()));
				}
			}
		} finally {
			long execTime = tt.stop();
			if (execTime < MAX_EXEC_TIME) {
				if (log.isDebugEnabled()) log.debug(String.format("Completed in %d ms", Long.valueOf(execTime)));
			} else
				log.warn(String.format("%s completed in %d ms", cmd.getID(), Long.valueOf(execTime)));

			// Create the command result statistics entry
			CommandLog cmdLog = new CommandLog(cmd.getID(), ctxt.getResult());
			cmdLog.setRemoteAddr(req.getRemoteAddr());
			cmdLog.setRemoteHost(req.getRemoteHost());
			cmdLog.setPilotID(ctxt.isAuthenticated() ? ctxt.getUser().getID() : 0);
			_cmdLogPool.add(cmdLog);
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (t != _logThread) {
			log.error(String.format("Unknown thread - %s", t.getName()), e);
			return;
		}
		
		_logThread = new CommandLogger(_maxCmdLogSize);
		_logThread.setUncaughtExceptionHandler(this);
		_logThread.start();
		log.error(String.format("Restarted %s", t.getName()), e);
	}
}