// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.security.Principal;
import javax.servlet.http.*;

import org.deltava.beans.Person;
import org.deltava.commands.CommandContext;

/**
 * A custom HTTP request wrapper to allow access to custom security information via standard Servlet API calls.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class CustomRequestWrapper extends HttpServletRequestWrapper {

    /**
     * Creates a new wrapper from a raw servlet request. 
     * @param req the HTTP Servlet Request
     */
    public CustomRequestWrapper(HttpServletRequest req) {
        super(req);
    }
    
    /**
     * Returns the authentication type.
     * @return HttpServletRequest.FORM_AUTH
     */
    public final String getAuthType() {
        return HttpServletRequest.FORM_AUTH;
    }

    /**
     * Returns the name of the logged in user.
     * @return the User name, or null if not authenticated
     * @see CustomRequestWrapper#getUserPrincipal()
     */
    public final String getRemoteUser() {
        Principal p = getUserPrincipal();
        return (p == null) ? null : p.getName();
    }
    
    /**
     * Returns the user object associated with the logged in user. Since Person implements Principal, this value
     * can be safely casted.
     * @return the Person object, or null if not authenticated
     * @see CustomRequestWrapper#getRemoteUser()
     * @see Person
     * @see CommandContext#USER_ATTR_NAME
     */
    public final Principal getUserPrincipal() {
        HttpSession s = getSession(false);
        return (s == null) ? null : (Person) s.getAttribute(CommandContext.USER_ATTR_NAME);
    }
    
    /**
     * Checks if a user is a member of a particular role. Unauthenticated users are members of the "anonymous" role.
     * @param roleName the role name
     * @return TRUE if the user is a member of the role, otherwise FALSE
     * @see Person#isInRole(String)
     */
    public final boolean isUserInRole(String roleName) {
    	// Always match the wildcard
    	if ("*".equals(roleName))
    		return true;
    	
        HttpSession s = getSession(false);
        if (s == null)
            return ("Anonymous".equals(roleName));
        
        // Get the person object
        Person p = (Person) s.getAttribute(CommandContext.USER_ATTR_NAME);
        return (p == null) ? ("Anonymous".equals(roleName)) : p.isInRole(roleName);
    }
}