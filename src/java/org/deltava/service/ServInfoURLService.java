// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.io.PrintWriter;

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
      
      // Set the content type
      ctx.getResponse().setContentType("text/plain");

      // Dump the URL list
		try {
		   PrintWriter pw = ctx.getResponse().getWriter();
			pw.println(DATA_HEADER);
			pw.println("url0=http://" + ctx.getRequest().getServerName() + "/sidata.ws");
			pw.println(";");
			pw.println("; END");
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}
		
		// Return result code
		return HttpServletResponse.SC_OK;
   }
}