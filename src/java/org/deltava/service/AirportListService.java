// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.GetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process Airport List AJAX requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportListService extends WebDataService {
   
   private interface AirportFilter {
      public boolean accept(Airport a);
   }
   
   private class AirlineFilter implements AirportFilter {
      
      private Airline _a;
      
      AirlineFilter(Airline a) {
         super();
         _a = (a == null) ? SystemData.getAirline(SystemData.get("airline.code")) : a;
      }
      
      public boolean accept(Airport a) {
         return a.getAirlineCodes().contains(_a.getCode());
      }
   }

   private class AirportListFilter implements AirportFilter {
      
      private Collection _airportCodes;
      
      AirportListFilter(Collection airports) {
         super();
         _airportCodes = new HashSet();
         for (Iterator i = airports.iterator(); i.hasNext(); ) {
            Airport a = (Airport) i.next();
            _airportCodes.add(a.getIATA());
         }
      }
      
      public boolean accept(Airport a) {
         return _airportCodes.contains(a.getIATA());
      }
   }
   
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // Figure out what kind of search we are doing
      AirportFilter filter = null;
      if (ctx.getParameter("airline") != null) {
         filter = new AirlineFilter(SystemData.getAirline(ctx.getParameter("airline").toUpperCase()));
      } else if (ctx.getParameter("code") != null) {
         // Check if we are searching origin/departure
         boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
         Airport a = SystemData.getAirport(ctx.getParameter("code").toUpperCase());
         if (a == null)
            throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Invalid Airport");
         
         // Get the airports from the schedule database
         try {
            GetSchedule dao = new GetSchedule(_con);
            filter = new AirportListFilter(dao.getConnectingAirports(a, !isDest));
         } catch (DAOException de) {
            throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
         }
      }
      
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
      
      // Generate the destination list
      Map allAirports = (Map) SystemData.getObject("airports");
      Collection airports = new TreeSet(allAirports.values());
      for (Iterator i = airports.iterator(); i.hasNext(); ) {
         Airport a = (Airport) i.next();
         if (filter.accept(a)) {
            Element e = new Element("airport");
            e.setAttribute("iata", a.getIATA());
            e.setAttribute("icao", a.getICAO());
            e.setAttribute("lat", StringUtils.format(a.getLatitude(), "##0.0000"));
            e.setAttribute("lng", StringUtils.format(a.getLongitude(), "##0.0000"));
            e.setAttribute("name", a.getName());
            re.addContent(e);
         }
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

		// Return success code
		return HttpServletResponse.SC_OK;
   }

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}