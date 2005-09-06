package org.deltava.servlet;

import java.sql.Connection;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.CommandLog;
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

public class CommandServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CommandServlet.class);
    
    private ConnectionPool _jdbcPool;
    private Map _cmds;

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
           _cmds = CommandFactory.load(SystemData.get("config.commands"), getServletContext());
        } catch (IOException ie) {
           throw new ServletException(ie);
        }
        
        // Initialize the redirection command
        Command cmd = new RedirectCommand();
        try {
           cmd.setContext(getServletContext());
           cmd.init("$redirect", "Request Redirection");
           cmd.setRoles(Arrays.asList(new String[] {"*"}));
           _cmds.put(cmd.getID(), cmd);
        } catch (CommandException ce) {
           throw new ServletException(ce);
        }
        
        // Save the connection pool
        _jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
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
    private Command getCommand(String rawURL) throws CommandException {
        URLParser parser = new URLParser(rawURL);
        String cmdName = parser.getName().toLowerCase();

        // Fetch the command from the map
        Command cmd = (Command) _cmds.get(cmdName);
        if (cmd == null)
            throw new CommandException("Command " + cmdName + " not found");

        return cmd;
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

        Command cmd = null;
        try {
            cmd = getCommand(req.getRequestURI());
            CommandContext ctxt = new CommandContext(req, rsp);
            
            // Validate command access
            if (!RoleUtils.hasAccess(ctxt.getRoles(), cmd.getRoles()))
                throw new CommandSecurityException("Not Authorized to execute", cmd.getName());
            
            // If we are not executing the redirection command, clear the redirection state data in the session
            if (!(cmd instanceof RedirectCommand))
               RequestStateHelper.clear(req);

            // Execute the command
            log.debug("Executing " + req.getMethod() + " " + cmd.getName());
            cmd.execute(ctxt);
            ctxt.setCacheHeaders();
            CommandResult result = ctxt.getResult();
            result.complete();

            // Create the command result statistics entry
            CommandLog cmdLog = new CommandLog(cmd.getID(), result);
            cmdLog.setRemoteAddr(req.getRemoteAddr());
            cmdLog.setRemoteHost(req.getRemoteHost());
            
            // Log the command statistics
            Connection c = null;
            try {
            	c = _jdbcPool.getSystemConnection();
            	SetSystemData swdao = new SetSystemData(c);
            	swdao.logCommand(cmdLog);
            } catch (DAOException de) {
            	log.warn("Error writing command result staitistics - " + de.getMessage());
            } finally {
            	_jdbcPool.release(c);
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
        } catch (CommandSecurityException cse) {
        	// Get the user name
        	String usrName = (req.getUserPrincipal() == null) ? "Anonymous" : req.getUserPrincipal().getName();
        	log.error("Security Error - " + usrName + " executing " + cse.getCommand() + " - " + cse.getMessage());
        	RequestDispatcher rd = req.getRequestDispatcher("/jsp/securityViolation.jsp");
        	rd.forward(req, rsp);
        } catch (CommandException ce) {
            log.error("Error executing command - " + ce.getMessage(), ce);
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/error.jsp");
            req.setAttribute("servlet_error", ce.getMessage());
            req.setAttribute("servlet_exception", (ce.getCause() == null) ? ce : ce.getCause());
            rd.forward(req, rsp);
        } finally {
        	long execTime = System.currentTimeMillis() - startTime;
        	if (execTime < 20000) {
        		log.debug("Completed in " + String.valueOf(execTime) + " ms");
        	} else {
        		log.warn(cmd.getID() + " completed in " + String.valueOf(execTime) + " ms");
        	}
        }
    }
}