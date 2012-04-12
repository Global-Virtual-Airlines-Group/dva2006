// Copyright 2006, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Service to display Pilot Locations on a map.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class PilotLocationService extends WebService {
	
	private final Random rnd = new Random();

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		boolean isHR = ctx.isUserInRole("HR");

		// Calculate the random location adjuster (between -1.5 and +1.5)
		double rndAmt = ((rnd.nextDouble() * 3) - 1) / GeoLocation.DEGREE_MILES;

		// Get active pilots and their locations
		Collection<Pilot> pilots = null;
		Map<Integer, GeoLocation> locations = null;
		try {
			Connection con = ctx.getConnection();
			GetPilot dao = new GetPilot(con);
			GetPilotBoard pbdao = new GetPilotBoard(con);
			locations = pbdao.getActive();
			pilots = dao.getActivePilots(null);
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
				
		// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
			Pilot usr = i.next();
			GeoLocation gl = locations.get(new Integer(usr.getID()));
			if (gl != null) {
				GeoPosition gp = new GeoPosition(gl);
				gp.setLatitude(gp.getLatitude() + rndAmt);
				gp.setLongitude(gp.getLongitude() + rndAmt);
					
				// Init the location bean
				PilotLocation loc = new PilotLocation(usr, gp);
				loc.setAllowDelete(isHR);
				
				// Build the element
				Element e = XMLUtils.createElement("pilot", loc.getInfoBox(), true);
				e.setAttribute("id", String.valueOf(usr.getID()));
				e.setAttribute("rank", usr.getRank().getName());
				e.setAttribute("eqType", usr.getEquipmentType());
				e.setAttribute("minZoom", "1");
				e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
				e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
				e.setAttribute("color", loc.getIconColor());
				re.addContent(e);
			}
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}