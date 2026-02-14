// Copyright 2005, 2007, 2009, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.security.Principal;
import jakarta.servlet.http.*;

import org.deltava.beans.Person;

import org.deltava.commands.HTTPContext;

/**
 * A custom HTTP request wrapper to allow access to custom security information via standard Servlet API calls.
 * @author Luke
 * @version 12.4
 * @since 1.0
 */

public class CustomRequestWrapper extends HttpServletRequestWrapper {
	
	private final String _hostName;

    /**
     * Creates a new wrapper from a raw servlet request. 
     * @param req the HTTP Servlet Request
     * @param remoteHost the remote host name
     */
    public CustomRequestWrapper(HttpServletRequest req, String remoteHost) {
        super(req);
        _hostName = remoteHost;
    }
    
    @Override
    public final String getAuthType() {
        return HttpServletRequest.FORM_AUTH;
    }

    @Override
    public final String getRemoteUser() {
        Principal p = getUserPrincipal();
        return (p == null) ? null : p.getName();
    }
    
    @Override
	public final String getRemoteHost() {
    	return _hostName;
    }
    
    /**
     * Returns the user object associated with the logged in user. Since Person implements Principal, this value can be safely casted.
     * @return the Person object, or null if not authenticated
     * @see CustomRequestWrapper#getRemoteUser()
     * @see HTTPContext#USER_ATTR_NAME
     */
    @Override
    public final Principal getUserPrincipal() {
        HttpSession s = super.getSession(false);
        return (s == null) ? null : (Person) s.getAttribute(HTTPContext.USER_ATTR_NAME);
    }
    
    /**
     * Checks if a user is a member of a particular role. Unauthenticated users are members of the "anonymous" role.
     * @param roleName the role name
     * @return TRUE if the user is a member of the role, otherwise FALSE
     * @see Person#isInRole(String)
     */
    @Override
    public final boolean isUserInRole(String roleName) {
    	// Always match the wildcard
    	if ("*".equals(roleName))
    		return true;
    	
        HttpSession s = super.getSession(false);
        if (s == null)
            return ("Anonymous".equals(roleName));
        
        // Get the person object
        Person p = (Person) s.getAttribute(HTTPContext.USER_ATTR_NAME);
        return (p == null) ? ("Anonymous".equals(roleName)) : p.isInRole(roleName);
    }
}