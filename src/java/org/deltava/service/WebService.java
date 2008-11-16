// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import org.deltava.util.StringUtils;

/**
 * Web Services are designed to be light-weight objects that are instantiated using a no-argument constructor
 * and then passed a request and a response.
 * @author Luke
 * @version 2.3
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
   
   /**
    * Creates a ServiceException. This method allows subclasses outside the default package to create a new
    * ServiceException, which has a package-private constructor.
    * @param code the HTTP code
    * @param msg the error message
    * @return ServiceException the newly-created ServiceException
    * @see ServiceException#ServiceException(int, String)
    */
   protected ServiceException error(int code, String msg) {
	   return new ServiceException(code, msg, true);
   }
   
   /**
    * Creates a ServiceException. This method allows subclasses outside the default package to create a new
    * ServiceException, which has a package-private constructor.
    * @param code the HTTP code
    * @param msg the error message
    * @param dumpStack TRUE if a stack dump should be logged, otherwise FALSE
    * @return ServiceException the newly-created ServiceException
    * @see ServiceException#ServiceException(int, String)
    */
   protected ServiceException error(int code, String msg, boolean dumpStack) {
	   return new ServiceException(code, msg, dumpStack);
   }

   /**
    * Creates a ServiceException. This method allows subclasses outside the default package to create a new
    * ServiceException, which has a package-private constructor.
    * @param code the HTTP code
    * @param msg the error message
    * @param t the root exception
    * @return ServiceException the newly-created ServiceException
    * @see ServiceException#ServiceException(int, String, Throwable)
    */
   protected ServiceException error(int code, String msg, Throwable t) {
	   return new ServiceException(code, msg, t);
   }
   
	/**
	 * Helper method to return the number of entries to display.
	 * @param sctxt the Service Context
	 * @param defaultValue the default number of entries
	 * @return the value of the count parameter, or defaultVlue
	 */
	protected int getCount(ServiceContext sctxt, int defaultValue) {
		return StringUtils.parse(sctxt.getRequest().getParameter("count"), defaultValue);
	}
}