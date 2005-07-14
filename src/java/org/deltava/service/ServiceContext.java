// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.deltava.beans.Person;
import org.deltava.security.SecurityContext;

/**
 * An invocation/security context object for Web Services.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServiceContext implements Serializable, SecurityContext {
   
   // List of roles for anonymous users
   private static final List ANONYMOUS_ROLES = Arrays.asList(new String[] { "Anonymous" } );

   private Person _usr;
   
   private ServletContext _sc;
   private HttpServletRequest _req;
   private HttpServletResponse _rsp;
   
   /**
    * Intiailizes the Web Service context.
    * @param req the HTTP servlet request
    * @param rsp the HTTP servlet response
    * @see ServiceContext#getRequest()
    * @see ServiceContext#getResponse()
    */
   public ServiceContext(HttpServletRequest req, HttpServletResponse rsp, ServletContext sc) {
      super();
      _sc = sc;
      _req = req;
      _rsp = rsp;
   }

   /**
    * Returns if an authenticated user is executing this Web Service.
    * @return TRUE if the user is authenticated, otherwise FALSE
    * @see ServiceContext#getUser()
    */
   public boolean isAuthenticated() {
      return (_usr != null);
   }

   /**
    * Returns the User executing this Web Service.
    * @return the User object, or null if anonymous
    * @see ServiceContext#isAuthenticated()
    * @see ServiceContext#isUserInRole(String)
    */
   public Person getUser() {
      return _usr;
   }

   /**
    * Returns the security roles associated with this user.
    * @return a Collection of role names
    */
   public Collection getRoles() {
      return isAuthenticated() ? _usr.getRoles() : ANONYMOUS_ROLES;
   }
   
   /**
    * Queries if the user accessing this Web Service is a member of a particular Security Role.
    * @return TRUE if the user is in the role or the wildcard role (&quot;*quot;) is specified, otherwise
    * FALSE
    */
   public boolean isUserInRole(String roleName) {
      if (isAuthenticated())
         return _usr.isInRole(roleName);
      
      return ("*".equals(roleName) || ANONYMOUS_ROLES.contains(roleName));
   }
   
   /**
    * Updates the User executing this Web Service.
    * @param p the User object, or null if anonymous
    */
   public void setUser(Person p) {
      _usr = p;
   }
   
   /**
    * Returns the Servlet Context for this web service.
    * @return the servlet context
    */
   public ServletContext getServletContext() {
      return _sc;
   }
   
   /**
    * Returns the current HTTP servlet request.
    * @return the servlet request
    * @see ServiceContext#getResponse()
    */
   public HttpServletRequest getRequest() {
      return _req; 
   }
   
   /**
    * Returns the current HTTP servlet response.
    * @return the servlet response
    * @see ServiceContext#getRequest()
    */
   public HttpServletResponse getResponse() {
      return _rsp;
   }
}