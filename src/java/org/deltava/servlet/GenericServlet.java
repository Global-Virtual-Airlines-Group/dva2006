package org.deltava.servlet;

import java.util.*;

import javax.servlet.http.*;

import org.deltava.beans.Person;

import org.deltava.jdbc.ConnectionPool;
import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * A class storing common servlet helper methods.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class GenericServlet extends HttpServlet {
	
	protected class ServletSecurityContext implements SecurityContext {
		
		private final List<String> ANONYMOUS_ROLES = Arrays.asList(new String[] {"Anonymous"});
		
		private HttpServletRequest _req;
		
		public ServletSecurityContext(HttpServletRequest req) {
			super();
			_req = req;
		}
		
		public Person getUser() {
			return (Person) _req.getUserPrincipal();
		}
		
		public boolean isAuthenticated() {
			return (getUser() != null);
		}
		
		public Collection<String> getRoles() {
			return isAuthenticated() ? getUser().getRoles() : ANONYMOUS_ROLES;
		}
		
		public boolean isUserInRole(String roleName) {
			if ("*".equals(roleName))
				return true;
			
			return isAuthenticated() ? getUser().isInRole(roleName) : ANONYMOUS_ROLES.contains(roleName);
		}
		
		public HttpServletRequest getRequest() {
			return _req;
		}
	}

    /**
     * A helper method to get the JDBC Connection Pool.
     * @return the JDBC Connection Pool
     */
    protected ConnectionPool getConnectionPool() {
        return (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
    }
}