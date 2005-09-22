// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.GetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to search the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleSearchService extends WebDataService {
   
   /**
    * Executes the Web Service, returning a list of flights.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
	public int execute(ServiceContext ctx) throws ServiceException {
	   
      // Populate the search criteria from the request
      Airline a = SystemData.getAirline(ctx.getParameter("airline"));
      ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, parse(ctx.getParameter("flightNumber")),
              parse(ctx.getParameter("flightLeg")));
      criteria.setEquipmentType(ctx.getParameter("eqType"));
      criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
      criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
      criteria.setDistance(parse(ctx.getParameter("distance")));
      criteria.setMaxResults(parse(ctx.getParameter("maxResults")));
      if ((criteria.getMaxResults() == 0) || (criteria.getMaxResults() > 50))
          criteria.setMaxResults(30);

      // Get the DAO and execute the search
      List results = null;
      try {
         GetSchedule dao = new GetSchedule(_con);
         dao.setQueryMax(criteria.getMaxResults());
         results = dao.search(criteria, "FLIGHT");
      } catch (DAOException de) {
         throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
      }
      
      // Generate the XML document
      Document doc = new Document();
      Element re = new Element("wsdata");
      doc.setRootElement(re);
      
      // Create the list
      Element le  = new Element("flights");
      re.addContent(le);
      for (Iterator i = results.iterator(); i.hasNext(); ) {
         ScheduleEntry f = (ScheduleEntry) i.next();
         Element e = new Element("flight");
         e.setAttribute("airline", f.getAirline().getCode());
         e.setAttribute("flightNumber", StringUtils.format(f.getFlightNumber(), "#000"));
         e.setAttribute("leg", String.valueOf(f.getLeg()));
         e.setAttribute("historic", String.valueOf(f.isHistoric()));
         e.addContent(createElement("eqType", f.getEquipmentType()));
         e.addContent(createElement("length", String.valueOf(f.getLength() * 6)));
         
         // Create departure info
         Element de = new Element("origin");
         de.addContent(createElement("name", f.getAirportD().getName()));
         de.addContent(createElement("iata", f.getAirportD().getIATA()));
         de.addContent(createElement("icao", f.getAirportD().getICAO()));
         de.addContent(createElement("time", f.getDateTimeD().toString()));
         e.addContent(de);
         
         // Create arrival info
         Element ae = new Element("destination");
         ae.addContent(createElement("name", f.getAirportA().getName()));
         ae.addContent(createElement("iata", f.getAirportA().getIATA()));
         ae.addContent(createElement("icao", f.getAirportA().getICAO()));
         ae.addContent(createElement("time", f.getDateTimeA().toString()));
         e.addContent(ae);
         
         // Add to results
         le.addContent(e);
      }
      
      // Dump the XML to the output stream
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
      try {
         ctx.getResponse().setContentType("text/xml");
         ctx.println(xmlOut.outputString(doc));
         ctx.commit();
      } catch (IOException ie) {
         throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
      }

      // Write result code
      return HttpServletResponse.SC_OK;
	}

	/**
	 * Marks the Web Service as secure.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
	
   /**
    * Helper method to parse a numeric request parameter.
    */
   private int parse(String param) {
       if ("".equals(param))
           return 0;
       try {
           return Integer.parseInt(param);
       } catch (NumberFormatException nfe) {
           return 0;
       }
   }
}