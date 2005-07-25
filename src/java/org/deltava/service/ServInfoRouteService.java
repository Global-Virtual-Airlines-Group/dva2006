// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.net.*;
import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.GetNavData;
import org.deltava.dao.http.GetServInfo;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download ServInfo route data for Google Maps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServInfoRouteService extends WebDataService {
   
	private static final Logger log = Logger.getLogger(ServInfoRouteService.class);

	/**
	 * Helper method to open a connection to a particular URL.
	 */
	private HttpURLConnection getURL(String dataURL) throws IOException {
		URL url = new URL(dataURL);
		log.info("Loading data from " + url.toString());
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // Get the network name
      String networkName = ctx.getRequest().getParameter("network");
      if (networkName == null)
         networkName = SystemData.get("online.default_network");

		// Get VATSIM/IVAO data
      NetworkInfo info = null;
		try {
			// Connect to info URL
			HttpURLConnection urlcon = getURL(SystemData.get("online." + networkName.toLowerCase() + ".status_url"));

			// Get network status
			GetServInfo sdao = new GetServInfo(urlcon);
			NetworkStatus status = sdao.getStatus(networkName);
			urlcon.disconnect();
			
			// Get network status
			urlcon = getURL(status.getDataURL());
			GetServInfo idao = new GetServInfo(urlcon);
			idao.setBufferSize(32768);
			info = idao.getInfo(networkName);
			urlcon.disconnect();
		} catch (Exception e) {
		   log.error(e.getMessage(), e);
		   throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		// Get the Pilot
		Pilot p = info.getPilot(ctx.getRequest().getParameter("id"));
		
		// Build the waypoints and add the airports if necessary
		List waypoints = new ArrayList(p.getWayPoints());
		if (!waypoints.contains(p.getAirportD().getICAO()))
		   waypoints.add(0, p.getAirportA().getICAO());
		
		if (!waypoints.contains(p.getAirportA().getICAO()))
		   waypoints.add(p.getAirportA().getICAO());
		
		// Generate the XML document
		Document doc = new Document();
      Element re = new Element("wsdata");
      doc.setRootElement(re);

		try {
		   GetNavData dao = new GetNavData(_con);
		   Map wpdata = dao.getByID(waypoints);
		   
		   // Build each waypoint
		   for (Iterator i = waypoints.iterator(); i.hasNext(); ) {
		      String wpCode = (String) i.next();
		      NavigationDataBean navdata = (NavigationDataBean) wpdata.get(wpCode);
		      if (navdata != null) {
		         Element nde = new Element("navaid");
		         nde.setAttribute("id", navdata.getCode());
		         nde.setAttribute("color", navdata.getIconColor());
		         nde.setAttribute("lat", StringUtils.format(navdata.getLatitude(), "##0.00000"));
		         nde.setAttribute("lng", StringUtils.format(navdata.getLongitude(), "##0.00000"));
		         CDATA lTxt = new CDATA(navdata.getInfoBox());
		         nde.addContent(lTxt);
		         
		         // Add the element to the XML document
		         re.addContent(nde);
		      }
		   }
		} catch (DAOException de) {
		   log.error(de.getMessage(), de);
		   throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
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
}