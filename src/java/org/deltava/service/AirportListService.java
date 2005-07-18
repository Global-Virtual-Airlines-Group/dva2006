// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to retrieve airport data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportListService extends WebService {

   /**
    * Executes the Web Service, returning a list of airports.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // Get the Airport map and strip out dupes
      Map allAirports = (Map) SystemData.getObject("airports");
      Set airports = new TreeSet(allAirports.values());
      
      // Generate the data element
      Document doc = new Document();
      Element re = new Element("wsdata");
      doc.setRootElement(re);
      
      // Create the list
      Element le  = new Element("airports");
      re.addContent(le);
      for (Iterator i = airports.iterator(); i.hasNext(); ) {
         Airport a = (Airport) i.next();
         Element e = new Element("airport");
         e.setAttribute("iata", a.getIATA());
         e.setAttribute("icao", a.getICAO());
         e.setAttribute("name", a.getName());
         e.setAttribute("lat", String.valueOf(a.getLatitude()));
         e.setAttribute("lon", String.valueOf(a.getLongitude()));
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

   /**
    * Returns if the Web Service requires authentication.
    * @return TRUE
    */
   public final boolean isSecure() {
      return true;
   }
}