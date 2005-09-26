// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

/**
 * Web Services are designed to be light-weight objects that are instantiated using a no-argument constructor
 * and then passed a request and a response.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class WebService {

   /**
    * Executes the Web Service.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
   public abstract int execute(ServiceContext ctx) throws ServiceException;
   
   /**
    * Returns wether this web service requires authentication.
    * @return TRUE if authentication is required, otherwise FALSE
    */
   public boolean isSecure() {
      return false;
   }
   
   /**
    * Returns wether this web service calls are logged. High volume services like the Map/ServInfo route services
    * will not be logged.
    * @return TRUE if invocation logging should be performed by the servlet, otherwise FALSE  
    */
   public boolean isLogged() {
	   return true;
   }
}