// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import org.jdom.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract Web Service to store common map plotting code. 
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public abstract class MapPlotService extends WebService {

	/**
	 * Converts route points into an XML document.
	 * @param points a List of MapEntry beans
	 * @param doIcons render markers as icons if supported
	 * @return a JDOM XML document
	 */
	protected Document formatPoints(List<NavigationDataBean> points, boolean doIcons) {

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Calculate the distance and midpoint by taking the first/last waypoints
		GeoLocation mp = null;
		int distance = 500;
		if (points.size() > 1) {
			NavigationDataBean ndf = points.get(0);
			mp = ndf.getPosition().midPoint(points.get(points.size() - 1));
			distance = ndf.getPosition().distanceTo(points.get(points.size() - 1));
		} else if (points.size() == 1)
			mp = points.get(0);

		// Save the midpoint
		if (mp != null) {
			Element mpe = new Element("midpoint");
			mpe.setAttribute("lat", StringUtils.format(mp.getLatitude(), "##0.00000"));
			mpe.setAttribute("lng", StringUtils.format(mp.getLongitude(), "##0.00000"));
			mpe.setAttribute("distance", StringUtils.format(distance, "###0"));
			re.addContent(mpe);
		}

		// Write the entries
		for (Iterator<NavigationDataBean> i = points.iterator(); i.hasNext();) {
			NavigationDataBean entry = i.next();
			Element e = XMLUtils.createElement("pos", entry.getInfoBox(), true);
			e.setAttribute("code", entry.getCode());
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			if (doIcons) {
				e.setAttribute("pal", String.valueOf(entry.getPaletteCode()));
				e.setAttribute("icon", String.valueOf(entry.getIconCode()));
			}
			
			re.addContent(e);
		}

		// Return the document
		return doc;
	}
	
	/**
	 * Translates IATA to ICAO codes.
	 * @param code the IATA code
	 * @return the ICAO code
	 */
	protected String txIATA(String code) {
		if ((code != null) && (code.length() == 3)) {
			Airport aD = SystemData.getAirport(code);
			if (aD != null)
				return aD.getICAO();
		}
		
		return code;
	}
	
	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}