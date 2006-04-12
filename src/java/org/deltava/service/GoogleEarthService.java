// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import org.jdom.Element;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.*;

/**
 * An abstract class to support Web Services rendering ACARS data in Google Earth. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class GoogleEarthService extends WebDataService {

	protected static final GoogleEarthColor[] COLORS = {GoogleEarthColor.make(208, 184, 0), GoogleEarthColor.make(0, 205, 0),
		GoogleEarthColor.make(240, 48, 48), GoogleEarthColor.make(80, 192, 240), GoogleEarthColor.make(240, 16, 240),
		GoogleEarthColor.make(0, 240, 240), GoogleEarthColor.make(240, 240, 64) };
	
	protected static class GoogleEarthColor {

		private int _blue;
		private int _green;
		private int _red;
		private int _alpha;
		
		public static GoogleEarthColor make(int red, int green, int blue, int alpha) {
			return new GoogleEarthColor(red, green, blue, alpha);
		}
		
		public static GoogleEarthColor make(int red, int green, int blue) {
			return new GoogleEarthColor(red, green, blue, 80);
		}
		
		private GoogleEarthColor(int red, int green, int blue, int alpha) {
			super();
			_red = (red > 255) ? 255 : red;
			_green = (green > 255) ? 255: green;
			_blue = (blue > 255) ? 255 : blue;
			_alpha = (alpha > 255) ? 255 : alpha;
		}

		public int getRed() {
			return _red;
		}
		
		public int getGreen() {
			return _green;
		}
		
		public int getBlue() {
			return _blue;
		}
		
		public int getAlpha() {
			return _alpha;
		}
		
		/**
		 * Helper method to generate a hex string without 0x and two characters long.
		 */
		private String formatHex(int value) {
			String tmp = StringUtils.formatHex(value).substring(2);
			return (tmp.length() == 1) ? "0" + tmp : tmp;
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder(formatHex(_alpha));
			buf.append(formatHex(_blue));
			buf.append(formatHex(_green));
			buf.append(formatHex(_red));
			return buf.toString();
		}
		
		public GoogleEarthColor dim(float factor) {
			return new GoogleEarthColor(Math.round(_red / factor), Math.round(_green / factor),
					Math.round(_blue / factor), Math.round(_alpha / factor));
		}
	}
	
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
		ade.addContent(KMLUtils.createLookAt(a, a.getAltitude() + 1000, -1));
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
		StringBuilder buf = new StringBuilder(StringUtils.format(entry.getAltitude(), "#,##0"));
		buf.append(" ft, ");
		buf.append(StringUtils.format(entry.getGroundSpeed(), "#,##0"));
		buf.append(" kts");
		ae.addContent(XMLUtils.createElement("Snippet", buf.toString()));
		
		// Build the icon
		Element ads = new Element("Style");
		Element ais = KMLUtils.createIcon(2, 0, 0); // airplane icon
		ais.addContent(XMLUtils.createElement("scale", "0.70"));
		ais.addContent(XMLUtils.createElement("heading", StringUtils.format(entry.getHeading(), "##0.00")));
		ads.addContent(ais);
		ae.addContent(ads);

		// Create the actual point
		Element pe = new Element("Point");
		pe.addContent(KMLUtils.createLookAt(entry, entry.getAltitude() + 1500, entry.getHeading()));
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
		ls.addContent(XMLUtils.createElement("PolyStyle", "color", routeColor.dim(3).toString()));
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
			pe.addContent(KMLUtils.createLookAt(entry, entry.getAltitude(), entry.getHeading()));
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
		Element rlce = XMLUtils.createElement("LineStyle", "color", GoogleEarthColor.make(224, 192, 192, 48).toString());
		rlce.addContent(XMLUtils.createElement("width", "2"));
		rls.addContent(rlce);
		rle.addContent(rls);
		Element rlse = new Element("LineString");
		rlse.addContent(XMLUtils.createElement("tessellate", "1"));
		rlse.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));

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
			pe.addContent(XMLUtils.createElement("visibility", isVisible ? "1" : "0"));
			Element pp = new Element("Point");
			pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(wp, 10)));
			pp.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
			pe.addContent(pp);
			pe.addContent(KMLUtils.createLookAt(wp, 4500, rnd.nextInt(360)));
			
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
		fe.addContent(rle);
		
		// Return the element
		return fe;
	}
}