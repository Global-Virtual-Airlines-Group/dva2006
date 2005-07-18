// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * A Web Service to list the ServInfo-compatible URLs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServInfoURLService extends WebService {
   
   private static final String DATA_HEADER = "120180:NOTCP";

   /**
	 * Executes the Web Service, returning a ServInfo data URL list.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // Build the URL list
      ctx.println(DATA_HEADER);
      ctx.println("url0=http://" + ctx.getRequest().getServerName() + "/sidata.ws");
      ctx.println(";");
      ctx.println("; END");
      
      // Dump the URL list
		try {
		   ctx.getResponse().setContentType("text/plain");
		   ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}
		
		// Return result code
		return HttpServletResponse.SC_OK;
   }
}