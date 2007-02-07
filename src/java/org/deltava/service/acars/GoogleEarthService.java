// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;

import org.jdom.Element;

import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.service.WebService;

import org.deltava.util.*;
import org.deltava.util.color.GoogleEarthColor;

/**
 * An abstract class to support Web Services rendering ACARS data in Google Earth. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class GoogleEarthService extends WebService {

	protected static final GoogleEarthColor[] COLORS = {new GoogleEarthColor(208, 184, 0), new GoogleEarthColor(0, 205, 0),
		new GoogleEarthColor(240, 48, 48), new GoogleEarthColor(80, 192, 240), new GoogleEarthColor(240, 16, 240),
		new GoogleEarthColor(0, 240, 240), new GoogleEarthColor(240, 240, 64) };
	
	/**
	 * Helper method to generate an airport Placemark element.
	 * @param a the Airport to render
	 * @param desc the placemark description
	 * @return a KML Placemark element
	 */
	protected Element createAirport(Airport a, String desc) {
		
		// Create the elemnet
		Element ade = new Element("Placemark");
		ade.addContent(XMLUtils.createElement("description", desc));
		ade.addContent(XMLUtils.createElement("name", a.getName() + " (" + a.getICAO() + ")"));
		ade.addContent(XMLUtils.createElement("visibility", "1"));
		Element ads = new Element("Style");
		ads.addContent(KMLUtils.createIcon(2, 0, 1)); // airplane icon
		ads.addContent(XMLUtils.createElement("scale", "0.55"));
		ade.addContent(ads);
		Element adp = new Element("Point");
		adp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(a)));
		ade.addContent(adp);
		ade.addContent(KMLUtils.createLookAt(a, a.getAltitude() + 3500, 1, 10));
		return ade;
	}
	
	/**
	 * Helper method to generate an aircraft Placemark element.
	 * @param name the entry title/name
	 * @param entry the position/flight data
	 * @return a KML Placemark element
	 */
	protected Element createAircraft(String name, RouteEntry entry) {
		
		// Create the elemnet
		Element ae = new Element("Placemark");
		ae.addContent(XMLUtils.createElement("description", entry.getInfoBox(), true));
		ae.addContent(XMLUtils.createElement("name", name));
		ae.addContent(XMLUtils.createElement("visibility", "1"));
		ae.addContent(KMLUtils.createLookAt(entry, entry.getAltitude() * 2 + 2000, entry.getHeading() - 140, 15));
		StringBuilder buf = new StringBuilder(StringUtils.format(entry.getAltitude(), "#,##0"));
		buf.append(" ft, ");
		buf.append(StringUtils.format(entry.getGroundSpeed(), "#,##0"));
		buf.append(" kts");
		ae.addContent(XMLUtils.createElement("Snippet", buf.toString()));
		
		// Build the icon
		Element ads = new Element("Style");
		Element ais = KMLUtils.createIcon(2, 0, 0); // airplane icon
		ais.addContent(XMLUtils.createElement("scale", "0.60"));
		ais.addContent(XMLUtils.createElement("heading", StringUtils.format(entry.getHeading(), "##0.00")));
		ads.addContent(ais);
		ae.addContent(ads);

		// Create the actual point
		Element pe = new Element("Point");
		if (entry.isFlagSet(ACARSFlags.FLAG_ONGROUND)) {
			pe.addContent(XMLUtils.createElement("coordinates", GeoUtils.format2D(entry)));
			pe.addContent(XMLUtils.createElement("altitudeMode", "clampedToGround"));
		} else if (entry.getRadarAltitude() < 1000) {
			pe.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry.getRadarAltitude())));
			pe.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
		} else {
			pe.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry.getAltitude())));
			pe.addContent(XMLUtils.createElement("altitudeMode", "absolute"));
		}
		
		// Add the point and return
		ae.addContent(pe);
		return ae;
	}
	
	/**
	 * Helper method to render flight progress as a KML Placemark / LineString element.
	 * @param positions a Collection of RouteEntry beans
	 * @param routeColor the line/fill color
	 * @return a KML Placemark element containing the flight progress
	 */
	protected Element createProgress(Collection<RouteEntry> positions, GoogleEarthColor routeColor) {
		
		// Set the placemark options
		Element le = new Element("Placemark");
		le.addContent(XMLUtils.createElement("name", "Flight Route"));
		le.addContent(XMLUtils.createElement("description", String.valueOf(positions.size()) + " Position Records"));
		le.addContent(XMLUtils.createElement("visibility", "1"));
		Element ls = new Element("Style");
		Element lce = XMLUtils.createElement("LineStyle", "color", routeColor.toString());
		lce.addContent(XMLUtils.createElement("width", "3"));
		ls.addContent(lce);
		ls.addContent(XMLUtils.createElement("PolyStyle", "color", routeColor.dim(2.5f).toString()));
		le.addContent(ls);
		Element lse = new Element("LineString");
		lse.addContent(XMLUtils.createElement("extrude", "1"));
		lse.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
		lse.addContent(XMLUtils.createElement("tessellate", "1"));

		// Format all of the coordinates
		StringBuilder buf = new StringBuilder();
		for (Iterator<RouteEntry> i = positions.iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			buf.append(GeoUtils.format3D(entry, entry.getRadarAltitude()));
			buf.append(" \n");
		}

		// Save the coordinates and return
		lse.addContent(XMLUtils.createElement("coordinates", buf.toString()));
		le.addContent(lse);
		return le;
	}
	
	/**
	 * Helper method to generate flight position data as a KML Folder element.
	 * @param positions a Collection of PositionEntry beans
	 * @param isVisible TRUE if the data points should be visible, otherwise FALSE 
	 * @return a KML Folder element
	 */
	protected Element createPositionData(Collection<RouteEntry> positions, boolean isVisible) {
		
		// Create the folder
		Element fe = new Element("Folder");
		fe.addContent(XMLUtils.createElement("name", "Position Records"));
		fe.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
		
		// Render the position data
		int pos = 0;
		for (Iterator<RouteEntry> i = positions.iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			Element pe = new Element("Placemark");
			pe.addContent(XMLUtils.createElement("name", "Route Point #" + String.valueOf(++pos)));
			pe.addContent(XMLUtils.createElement("description", entry.getInfoBox(), true));
			pe.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
			pe.addContent(XMLUtils.createElement("Snippet", StringUtils.format(entry, false, GeoLocation.ALL)));
			pe.addContent(KMLUtils.createLookAt(entry, entry.getAltitude() * 2 + 2000, entry.getHeading() - 140, 15));
			Element ps = new Element("Style");
			Element pis = KMLUtils.createIcon(2, 0, 0); // info icon
			pis.addContent(XMLUtils.createElement("scale", "0.70"));
			pis.addContent(XMLUtils.createElement("heading", StringUtils.format(entry.getHeading(), "##0.00")));
			ps.addContent(pis);
			pe.addContent(ps);
			Element pp = new Element("Point");
			if (entry.isFlagSet(ACARSFlags.FLAG_ONGROUND)) {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format2D(entry)));
				pp.addContent(XMLUtils.createElement("altitudeMode", "clampedToGround"));
			} else if (entry.getRadarAltitude() < 1000) {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry
						.getRadarAltitude())));
				pp.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
			} else {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry.getAltitude())));
				pp.addContent(XMLUtils.createElement("altitudeMode", "absolute"));
			}

			pe.addContent(pp);
			fe.addContent(pe);
		}

		// Return the folder element
		return fe;
	}
	
	/**
	 * Renders the flight plan to Google Earth KML. 
	 * @param name the Flight Plan KML folder name
	 * @param waypoints a Collection of NavigationDataBeans
	 * @param isVisible TRUE if the element is visible, otherwise FALSE
	 * @return a KML Folder element containing the flight plan route
	 */
	protected Element createFlightRoute(String name, Collection<NavigationDataBean> waypoints, boolean isVisible) {

		// Create the route folder
		Element fe = new Element("Folder");
		fe.addContent(XMLUtils.createElement("name", name));
		fe.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
		
		// Create route line
		Element rle = new Element("Placemark");
		rle.addContent(XMLUtils.createElement("name", "Flight Route"));
		rle.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
		Element rls = new Element("Style");
		Element rlce = XMLUtils.createElement("LineStyle", "color", new GoogleEarthColor(224, 192, 192, 48).toString());
		rlce.addContent(XMLUtils.createElement("width", "2"));
		rls.addContent(rlce);
		rle.addContent(rls);
		Element rlse = new Element("LineString");
		rlse.addContent(XMLUtils.createElement("tessellate", "1"));
		rlse.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
		fe.addContent(rle);

		// Loop through the points
		Random rnd = new Random();
		StringBuilder pbuf = new StringBuilder();
		Collection<String> routeText = new LinkedHashSet<String>();
		for (Iterator<NavigationDataBean> i = waypoints.iterator(); i.hasNext(); ) {
			NavigationDataBean wp = i.next();
			routeText.add(wp.getCode());
			
			Element pe = new Element("Placemark");
			pe.addContent(XMLUtils.createElement("name", wp.getCode()));
			pe.addContent(XMLUtils.createElement("description", wp.getInfoBox(), true));
			pe.addContent(XMLUtils.createElement("Snippet", wp.getTypeName()));
			pe.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
			pe.addContent(KMLUtils.createLookAt(new GeoPosition(wp), 4500, rnd.nextInt(360), 10));
			Element pp = new Element("Point");
			pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(wp, 10)));
			pp.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
			pe.addContent(pp);
			
			// Format route point
			pbuf.append(GeoUtils.format3D(wp, 0));
			pbuf.append(" \n");

			// Add icon
			Element ps = new Element("Style");
			Element pis = null;
			switch (wp.getType()) {
				case NavigationDataBean.NDB:
				case NavigationDataBean.VOR:
					pis = KMLUtils.createIcon(4, 0, 1);
					pis.addContent(XMLUtils.createElement("scale", "0.60"));
					ps.addContent(pis);
					pe.addContent(ps);
					break;
					
				case NavigationDataBean.INT:
					pis = KMLUtils.createIcon(4, 4, 0); // info icon
					pis.addContent(XMLUtils.createElement("scale", "0.50"));
					ps.addContent(pis);
					pe.addContent(ps);
					break;
			}

			if (pis != null)
				fe.addContent(pe);
		}
		
		// Save the route text
		rle.addContent(XMLUtils.createElement("description", StringUtils.listConcat(routeText, " "), true));
		
		// Save the coordinates
		rlse.addContent(XMLUtils.createElement("coordinates", pbuf.toString()));
		rle.addContent(rlse);
		
		// Return the element
		return fe;
	}
}