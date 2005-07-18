// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to retrieve airline data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineListService extends WebService {

   /**
    * Executes the Web Service, returning a list of Airline names and codes.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // Get the Airport map and strip out dupes
      Map airlines = (Map) SystemData.getObject("airlines");
      
      // Generate the data element
      Document doc = new Document();
      Element re = new Element("wsdata");
      doc.setRootElement(re);
      
      // Create the list
      Element le  = new Element("airline");
      re.addContent(le);
      for (Iterator i = airlines.values().iterator(); i.hasNext(); ) {
         Airline a = (Airline) i.next();
         Element e = new Element("airlines");
         e.setAttribute("code", a.getCode());
         e.setAttribute("name", a.getName());
         le.addContent(e);
      }
      
      // Dump the XML to the output stream
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      try {
         ctx.getResponse().setContentType("text/xml");
         ctx.println(xmlOut.outputString(doc));
         ctx.commit();
      } catch (IOException ie) {
         throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
      }
      
      return HttpServletResponse.SC_OK;
   }
}