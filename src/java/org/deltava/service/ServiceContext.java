// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.deltava.beans.Person;

import org.deltava.jdbc.*;

import org.deltava.security.SecurityContext;

/**
 * An invocation/security context object for Web Services.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class ServiceContext extends ConnectionContext implements SecurityContext {
   
   // List of roles for anonymous users
   private static final Collection<String> ANONYMOUS_ROLES = Collections.singleton("Anonymous");
   
   private Person _usr;
   
   private ServletContext _sc;
   private HttpServletRequest _req;
   private HttpServletResponse _rsp;
   private final OutputBuffer _buf = new OutputBuffer();
   
   protected class OutputBuffer {
      
      private final StringBuilder _buffer = new StringBuilder(256);
      
      public void print(CharSequence value) {
         _buffer.append(value);
      }
      
      public void println(CharSequence value) {
    	 _buffer.append(value);
         _buffer.append("\r\n");
      }
      
      public int length() {
         return _buffer.length();
      }
      
      public String toString() {
         return _buffer.toString();
      }
   }
   
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
   public Collection<String> getRoles() {
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
    * Returns an HTTP request parameter.
    * @param paramName the parameter name
    * @return the parameter value, or null if not found
    */
   public String getParameter(String paramName) {
      return _req.getParameter(paramName);
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
   
   /**
    * Prints a string to the output buffer.
    * @param data the string to print
    * @see ServiceContext#println(String)
    * @see ServiceContext#commit()
    */
   public void print(String data) {
      _buf.print(data);
   }
   
   /**
    * Prints a string and a trailing newline to the output buffer.
    * @param data the string to print
    * @see ServiceContext#print(String)
    * @see ServiceContext#commit()
    */
   public void println(String data) {
      _buf.println(data);
   }
   
   /**
    * Writes the output buffer to the HTTP servlet response, setting the Content-length header.
    * @throws IOException if an I/O error occurs
    * @see ServiceContext#print(String)
    * @see ServiceContext#println(String)
    */
   public void commit() throws IOException {
      _rsp.setBufferSize((_buf.length() < 32768) ? _buf.length() + 16 : 32768);
      _rsp.setContentLength(_buf.length());
      _rsp.getWriter().print(_buf);
      _rsp.flushBuffer();
   }
}