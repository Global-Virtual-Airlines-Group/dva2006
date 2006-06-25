// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to search the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SearchService extends WebDataService {
   
   /**
    * Executes the Web Service, returning a list of flights.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
	public int execute(ServiceContext ctx) throws ServiceException {
	   
      // Populate the search criteria from the request
      Airline a = SystemData.getAirline(ctx.getParameter("airline"));
      ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, StringUtils.parse(ctx.getParameter("flightNumber"), 0),
              StringUtils.parse(ctx.getParameter("flightLeg"), 0));
      criteria.setEquipmentType(ctx.getParameter("eqType"));
      criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
      criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
      criteria.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
      criteria.setMaxResults(StringUtils.parse(ctx.getParameter("maxResults"), 0));
      if ((criteria.getMaxResults() == 0) || (criteria.getMaxResults() > 50))
          criteria.setMaxResults(30);

      // Get the DAO and execute the search
      List results = null;
      try {
         GetSchedule dao = new GetSchedule(_con);
         dao.setQueryMax(criteria.getMaxResults());
         results = dao.search(criteria, "FLIGHT");
      } catch (DAOException de) {
         throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
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
         e.setAttribute("historic", String.valueOf(f.getHistoric()));
         e.addContent(XMLUtils.createElement("eqType", f.getEquipmentType()));
         e.addContent(XMLUtils.createElement("length", String.valueOf(f.getLength() * 6)));
         
         // Create departure info
         Element de = new Element("origin");
         de.addContent(XMLUtils.createElement("name", f.getAirportD().getName()));
         de.addContent(XMLUtils.createElement("iata", f.getAirportD().getIATA()));
         de.addContent(XMLUtils.createElement("icao", f.getAirportD().getICAO()));
         de.addContent(XMLUtils.createElement("time", f.getDateTimeD().toString()));
         e.addContent(de);
         
         // Create arrival info
         Element ae = new Element("destination");
         ae.addContent(XMLUtils.createElement("name", f.getAirportA().getName()));
         ae.addContent(XMLUtils.createElement("iata", f.getAirportA().getIATA()));
         ae.addContent(XMLUtils.createElement("icao", f.getAirportA().getICAO()));
         ae.addContent(XMLUtils.createElement("time", f.getDateTimeA().toString()));
         e.addContent(ae);
         
         // Add to results
         le.addContent(e);
      }
      
      // Dump the XML to the output stream
      try {
         ctx.getResponse().setContentType("text/xml");
         ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
         ctx.commit();
      } catch (IOException ie) {
         throw error(SC_CONFLICT, "I/O Error");
      }

      // Write result code
      return SC_OK;
	}

	/**
	 * Marks the Web Service as secure.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
}