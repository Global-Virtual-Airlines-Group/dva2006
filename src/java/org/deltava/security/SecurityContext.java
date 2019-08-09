// Copyright 2005, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Pilot;

/**
 * An interface used by access controllers to query context data to determine access. 
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public interface SecurityContext {

   /**
    * Queries if the current user is authenticated or anonymous.
    * @return TRUE if a user is logged in, otherwise FALSE 
    */
   public boolean isAuthenticated();
   
   /**
    * Returns the currently authenticated user.
    * @return the user's Person object, or null if anonymous
    */
   public Pilot getUser();
   
   /**
    * Returns all the roles for the currently authenticated user.
    * @return a Collection of role names, or an empty collection
    */
   public Collection<String> getRoles();
   
   /**
    * Returns if the user is a member of a particular security role.
    * @param roleName the role name
    * @return TRUE if the user is a member of the role, otherwise FALSE
    */
   public boolean isUserInRole(String roleName);
   
   /**
    * Returns the current servlet request. Some access controllers may need to examine attributes
    * within the servlet request.
    * @return the HTTP servlet request
    */
   public HttpServletRequest getRequest();
}