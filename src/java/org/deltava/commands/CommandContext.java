// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.deltava.beans.Person;
import org.deltava.beans.FileUpload;

import org.deltava.jdbc.*;

import org.deltava.security.SecurityContext;
import org.deltava.util.system.SystemData;

/**
 * A class for storing run-time data needed for Command invocations. This class handles reserving and
 * releasing JDBC Connections, since by doing so we can easily return connections back to the pool in a
 * <b>finally</b> block without nasty scope issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see Command
 */

public class CommandContext implements java.io.Serializable, SecurityContext {

    // List of roles for anonymous users
    private static final List<String> ANONYMOUS_ROLES = Arrays.asList(new String[] { "Anonymous" } );
    
    public static final String AUTH_COOKIE_NAME = "authToken";
    public static final String USER_ATTR_NAME = "authUser";
    public static final String USRLISTENER_ATTR_NAME = "userSerializeListener";
    public static final String SU_ATTR_NAME ="superUser";
    public static final String SCREENX_ATTR_NAME = "screenXSize";
    public static final String SCREENY_ATTR_NAME = "screenYSize";
    public static final String ADDRINVALID_ATTR_NAME = "emailInvalid";

    private HttpServletRequest _req;
    private HttpServletResponse _rsp;

    private final CacheControl _cache = new CacheControl();
    private final CommandResult _result = new CommandResult(null);
    private Connection _con;

    /**
     * Creates a new Command context from an HTTP Servlet Request/Resposne pair.
     * @param req the Servlet Request
     * @param rsp the Servlet Response
     */
    public CommandContext(HttpServletRequest req, HttpServletResponse rsp) {
        super();
        _req = req;
        _rsp = rsp;
    }

    /**
     * Returns the current HTTP session.
     * @return the HTTP session, null if none present or invalid
     */
    public HttpSession getSession() {
        return _req.getSession(false);
    }

    /**
     * Returns the current HTTP Servlet Request.
     * @return the Servlet Request
     */
    public HttpServletRequest getRequest() {
        return _req;
    }

    /**
     * Returns the current HTTP Servlet Response.
     * @return the Servlet Response
     */
    public HttpServletResponse getResponse() {
        return _rsp;
    }

    /**
     * Returns the cache control options for the HTTP response.
     * @return the cache control bean
     * @see CommandContext#setCacheHeaders()
     */
    public CacheControl getCache() {
    	return _cache;
    }

    /**
     * Reserves a JDBC Connection from the connection pool.
     * @param isSystem if the command requires a system connection.
     * @return a JDBC Connection
     * @throws ConnectionPoolException if an error occurs
     * @throws IllegalStateException if a connection has already been reserved by this context
     * @see CommandContext#getConnection()
     * @see ConnectionPool#getConnection(boolean)
     * @see CommandContext#release()
     */
    public Connection getConnection(boolean isSystem) throws ConnectionPoolException {
        ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
        if (pool == null)
            throw new ConnectionPoolException("No Connection Pool defined", false);

        // Check if a connection has already been reserved
        if (_con != null)
            throw new IllegalStateException("Connection already reserved");

        _con = pool.getConnection(isSystem);
        return _con;
    }

    /**
     * Reserves a non-system JDBC Connection from the connection pool.
     * @return a JDBC Connection
     * @throws ConnectionPoolException if an error occurs
     * @throws IllegalStateException if a connection has already been reserved by this context
     * @see CommandContext#getConnection(boolean)
     * @see CommandContext#release()
     */
    public Connection getConnection() throws ConnectionPoolException {
        return getConnection(false);
    }
    
    /**
     * Returns wether an Administrator is impersonating another user.
     * @return TRUE if superuser mode is on, otherwise FALSE
     */
    public boolean isSuperUser() {
    	HttpSession s = _req.getSession(false);
    	return (s == null) ? false : (s.getAttribute(SU_ATTR_NAME) instanceof Person);
    }
    
    /**
     * Helper method to ensure a connection has been reserved.
     */
    private void checkConnection() {
       if (_con == null)
          throw new IllegalStateException("No JDBC Connection reserved");
    }
    
    /**
     * Starts a JDBC transaction block, by turning off autoCommit on the reserved Connection.
     * @throws IllegalStateException if no JDBC Connection is reserved
     * @throws TransactionException if a JDBC error occurs
     * @see CommandContext#commitTX()
     * @see CommandContext#rollbackTX()
     */
    public void startTX() throws TransactionException {
       checkConnection();
       try {
          _con.setAutoCommit(false);
       } catch (Exception e) {
          throw new TransactionException(e);
       }
    }

    /**
     * Commits the current JDBC transaction.
     * @throws IllegalStateException if no JDBC Connection is reserved
     * @throws TransactionException if a JDBC error occurs
     * @see CommandContext#startTX()
     * @see CommandContext#rollbackTX()
     */
    public void commitTX() throws TransactionException {
       checkConnection();
       try {
          _con.commit();
       } catch (Exception e) {
          throw new TransactionException(e);
       }
    }
    
    /**
     * Rolls back the current JDBC transaction. This will consume all exceptions.
     * @see CommandContext#startTX()
     * @see CommandContext#commitTX()
     */
    public void rollbackTX() {
       try {
          _con.rollback();
       } catch (Exception e) { }
    }

    /**
     * Returns a JDBC Connection to the connection pool.
     */
    public void release() {
        ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
        if (pool == null)
            return;

        // Return the connection and record the back-end usage
        _result.setBackEndTime(pool.release(_con));
        _con = null;
    }

    /**
     * Returns the Command Result object.
     * @return the results of the command invocation
     */
    public CommandResult getResult() {
        return _result;
    }

    /**
     * Returns if this command is being invoked by an authenticated user.
     * @return TRUE if the user is logged in, FALSE otherwise
     * @see CommandContext#getUser()
     * @see CommandContext#getRoles()
     */
    public boolean isAuthenticated() {
        return (_req.getRemoteUser() != null);
    }

    /**
     * Returns the authenticated user object.
     * @return the User object, null if not authenticated
     * @see CommandContext#isAuthenticated()
     * @see CommandContext#getRoles()
     * @see org.deltava.beans.Pilot
     * @see org.deltava.beans.Applicant
     */
    public Person getUser() {
        return (Person) _req.getUserPrincipal();
    }
    
    /**
     * Returns the list of roles for the user. This will return a List with a single element (&quot;Anonymous&quot;)
     * if the user is not currently authenticated
     * @return a Collection of role names
     * @see CommandContext#isAuthenticated()
     * @see CommandContext#getUser()
     */
    public Collection<String> getRoles() {
        return isAuthenticated() ? getUser().getRoles() : CommandContext.ANONYMOUS_ROLES;
    }
    
    /**
     * Returns if the currently logged in user is a member of a particular role. This method delegates the call to the
     * underlying HTTP servlet request's {@link HttpServletRequest#isUserInRole(String) } method, which may be
     * overriden by a custom request handler.
     * @param roleName the role name
     * @return TRUE if the user is a member of the specified role, otherwise FALSE
     */
    public boolean isUserInRole(String roleName) {
       return _req.isUserInRole(roleName);
    }

    /**
     * Passes a system message into the response.
     * @param msg the System Message
     */
    public void setMessage(String msg) {
        setAttribute("system_message", msg, Command.REQUEST);
    }

    /**
     * Sets an attribute in a particular context.
     * @param name the name of the attribute
     * @param value the attribute value
     * @param scope the scope (application, session or request)
     * @see Command#REQUEST
     * @see Command#SESSION
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case Command.SESSION:
                HttpSession s = _req.getSession(true);
                s.setAttribute(name, value);
                break;

            default:
            case Command.REQUEST:
                _req.setAttribute(name, value);
        }
    }

    /**
     * Returns one of the special command parameters.
     * @param prmType the Parameter type
     * @param defaultValue the value to return if parameter not specified in the request
     * @return the parameter value
     * @see Command#ID
     * @see Command#OPERATION
     */
    public Object getCmdParameter(int prmType, Object defaultValue) {
        switch (prmType) {
            case Command.OPERATION:
                String cmdOp = _req.getParameter("op");
                return (cmdOp == null) ? defaultValue : cmdOp;

            default:
            case Command.ID:
                String cmdID = _req.getParameter("id");
                if (cmdID == null) {
                    return defaultValue;
                } else if (cmdID.startsWith("0x")) {
                    try {
                        return new Integer(Integer.parseInt(cmdID.substring(2), 16));
                    } catch (NumberFormatException nfe) {
                        return cmdID;
                    }
                } else {
                    return cmdID;
                }
        }
    }

    /**
     * Returns a document ID as an integer.
     * @return the datbase ID, or 0 if not found
     * @throws CommandException if the ID cannot be parsed into a number
     */
    public int getID() throws CommandException {
        Object obj = getCmdParameter(Command.ID, new Integer(0));
        if (obj instanceof Integer)
            return ((Integer) obj).intValue();
        
        // Try and convert into an integer
        try {
        	return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            // Build a non-stackdumped exception
            CommandException ce = new CommandException("Invalid Database ID - " + obj);
            ce.setLogStackDump(false);
            throw ce;
        }
    }
    
    /**
     * Returns the value of a request parameter.
     * @param pName the parameter name
     * @return the parameter value, or null if not present
     */
    public String getParameter(String pName) {
        return _req.getParameter(pName);
    }
    
    /**
     * Returns the values of a collection of request parameters.
     * @param pName the parameter name
     * @return a Collection parameter values, or null if not present
     */
    public Collection<String> getParameters(String pName) {
    	String[] pValues = _req.getParameterValues(pName);
    	return (pValues == null) ? null : new ArrayList<String>(Arrays.asList(pValues));
    }
    
    /**
     * Returns the value of an uploaded file object. The
     * @param fName the file name
     * @return the file data, or null if not found
     */
    public FileUpload getFile(String fName) {
        return (FileUpload) _req.getAttribute("FILE$" + fName);
    }
    
    /**
     * Applies the current cache control strategy to the HTTP servlet response.
     * @see CommandContext#getCache()
     */
    public void setCacheHeaders() {
    	_rsp.setHeader("Cache-Control", _cache.isPublic() ? "public" : "private");
    	if (_cache.getMaxAge() != CacheControl.DEFAULT_CACHE) {
    		_rsp.setIntHeader("max-age", _cache.getMaxAge());
    		_rsp.setDateHeader("Expires", System.currentTimeMillis() + _cache.getMaxAge());
    	}
    }
}