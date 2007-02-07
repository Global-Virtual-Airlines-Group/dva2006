// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Service to display Pilot Locations on a map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotLocationService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Calculate the random location adjuster (between -1.5 and +1.5)
		Random rnd = new Random();
		double rndAmt = ((rnd.nextDouble() * 3) - 1) / GeoLocation.DEGREE_MILES;

		// Get active pilots
		Collection<PilotLocation> usrs = new HashSet<PilotLocation>();
		try {
			GetPilot dao = new GetPilot(ctx.getConnection());
			Map<Integer, GeoLocation> locations = dao.getPilotBoard();
			Collection<Pilot> pilots = dao.getByID(locations.keySet(), "PILOTS").values();

			// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
			for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
				Pilot usr = i.next();
				GeoPosition gp = new GeoPosition(locations.get(new Integer(usr.getID())));
				gp.setLatitude(gp.getLatitude() + rndAmt);
				gp.setLongitude(gp.getLongitude() + rndAmt);
				usrs.add(new PilotLocation(usr, gp));
			}
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Add the Map entries
		for (Iterator<PilotLocation> i = usrs.iterator(); i.hasNext();) {
			PilotLocation loc = i.next();
			Element e = XMLUtils.createElement("pilot", loc.getInfoBox(), true);
			e.setAttribute("rank", loc.getUser().getRank());
			e.setAttribute("eqType", loc.getUser().getEquipmentType());
			e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
			e.setAttribute("color", loc.getIconColor());
			re.addContent(e);
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
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