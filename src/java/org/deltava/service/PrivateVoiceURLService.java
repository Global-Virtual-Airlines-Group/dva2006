// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to return the URL of the Private Voice channel.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PrivateVoiceURLService extends WebService {

   /**
    * Executes the Web Service, returning the URL to the private voice channel.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
   public int execute(ServiceContext ctx) throws ServiceException {

      // Write the response
      ctx.println(SystemData.get("airline.voice.url"));
      try {
         ctx.getResponse().setContentType("text/plain");
         ctx.commit();
      } catch (IOException ie) {
         throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
      }

      return HttpServletResponse.SC_OK;
   }
}