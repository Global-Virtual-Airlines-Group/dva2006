// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import static org.deltava.beans.navdata.NavigationDataBean.*;

import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Service to format ACARS flight data for Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSEarthService extends WebDataService {
	
	private static final GoogleEarthColor[] COLORS = {GoogleEarthColor.make(0, 208, 184), GoogleEarthColor.make(0, 205, 0),
		GoogleEarthColor.make(240, 48, 48), GoogleEarthColor.make(240, 128, 80), GoogleEarthColor.make(240, 16, 240),
		GoogleEarthColor.make(0, 240, 240), GoogleEarthColor.make(240, 240, 64) };

	private static final Logger log = Logger.getLogger(ACARSEarthService.class);

	private static class GoogleEarthColor {

		private int _blue;
		private int _green;
		private int _red;
		private int _alpha;
		
		public static GoogleEarthColor make(int red, int green, int blue, int alpha) {
			return new GoogleEarthColor(red, green, blue, alpha);
		}
		
		public static GoogleEarthColor make(int red, int green, int blue) {
			return new GoogleEarthColor(red, green, blue, 60);
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
			StringBuilder buf = new StringBuilder(formatHex(_blue));
			buf.append(formatHex(_green));
			buf.append(formatHex(_red));
			buf.append(formatHex(_alpha));
			return buf.toString();
		}
		
		public GoogleEarthColor dim(int factor) {
			return new GoogleEarthColor(_red / factor, _green / factor, _blue / factor, _alpha / factor);
		}
	}
	
	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS Flight IDs
		Collection<Integer> IDs = new TreeSet<Integer>();
		Collection<String> rawIDs = StringUtils.split(ctx.getParameter("id"), ",");
		for (Iterator<String> i = rawIDs.iterator(); i.hasNext();) {
			String rawID = i.next();
			try {
				IDs.add(new Integer(StringUtils.parseHex(rawID)));
			} catch (NumberFormatException nfe) {
				log.warn("Invalid ACARS flight ID - " + rawID);
			}
		}

		// Check if we add position records
		boolean showData = Boolean.valueOf(ctx.getParameter("showData")).booleanValue();
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();
		
		// Get the DAOs
		GetACARSData dao = new GetACARSData(_con);
		GetNavData navdao = new GetNavData(_con);

		// Get ACARS data for the flights
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				FlightInfo info = dao.getInfo(id);
				if (info != null) {
					@SuppressWarnings("unchecked")
					Collection<RouteEntry> routeData = dao.getRouteEntries(id, true, info.getArchived());
					info.setRouteData(routeData);
					
					if (showRoute) {
						List<String> routeEntries = StringUtils.split(info.getRoute(), " ");
						NavigationDataMap navaids = navdao.getByID(routeEntries);
						GeoPosition lastWaypoint = new GeoPosition(info.getAirportD());
						int distance = lastWaypoint.distanceTo(info.getAirportA());
						
						// Filter out navaids and put them in the correct order
						Collection<NavigationDataBean> routeInfo = new LinkedHashSet<NavigationDataBean>();
						for (Iterator<String> ri = routeEntries.iterator(); ri.hasNext();) {
							String navCode = ri.next();
							NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
							if (wPoint != null) {
								if (lastWaypoint.distanceTo(wPoint) < distance) {
									routeInfo.add(wPoint);
									lastWaypoint.setLatitude(wPoint.getLatitude());
									lastWaypoint.setLongitude(wPoint.getLongitude());
								}
							}
						}

						info.setPlanData(routeInfo);
					}
					
					// Save the flight
					flights.add(info);
				} else {
					log.warn("Cannot find ACARS flight " + id);
				}
			}
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);
		
		// Add the ACARS data
		int colorOfs = -1;
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			Collection<Element> fData = createFlight(info, showData, COLORS[++colorOfs]);
			if (colorOfs >= COLORS.length)
				colorOfs = -1;
			
			Element fe = de;
			if (info.hasRouteData()) {
				fe = new Element("Folder");
				fe.addContent(XMLUtils.createElement("name", "Flight " + info.getID()));
				fe.addContent(XMLUtils.createElement("visibility", "1"));
				de.addContent(fe);
			}
			
			// Add the children
			for (Iterator<Element> ci = fData.iterator(); ci.hasNext(); )
				fe.addContent(ci.next());
		}

		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsFlights.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}

	/**
	 * Helper method to generate an icon element.
	 */
	private Element addIcon(int palette, int pX, int pY) {
		Element re = new Element("IconStyle");
		Element e = new Element("Icon");
		e.addContent(XMLUtils.createElement("href", "root://icons/palette-" + palette + ".png", true));
		e.addContent(XMLUtils.createElement("x", String.valueOf(pX * 32)));
		e.addContent(XMLUtils.createElement("y", String.valueOf(pY * 32)));
		e.addContent(XMLUtils.createElement("w", "32"));
		e.addContent(XMLUtils.createElement("h", "32"));
		re.addContent(e);
		return re;
	}

	/**
	 * Helper method to generate a LookAt element.
	 */
	private Element addLookAt(GeoLocation loc, int altitude, int heading) {
		Element e = new Element("LookAt");
		e.addContent(XMLUtils.createElement("longitude", StringUtils.format(loc.getLongitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("latitude", StringUtils.format(loc.getLatitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("range", StringUtils.format(0.3048d * altitude, "##0.000")));
		if (heading == -1) {
			e.addContent(XMLUtils.createElement("heading", "0.00"));
			e.addContent(XMLUtils.createElement("tilt", "85.0000"));
		} else {
			e.addContent(XMLUtils.createElement("heading", StringUtils.format(heading, "##0.00")));
			e.addContent(XMLUtils.createElement("tilt", "55.0000"));
		}

		return e;
	}

	/**
	 * Helper method to turn a flight record into a KML element.
	 */
	private Collection<Element> createFlight(FlightInfo info, boolean showPositionData, GoogleEarthColor routeColor) {

		// Create results
		Collection<Element> results = new ArrayList<Element>();

		// Generate the start airport placemark
		Airport aD = info.getAirportD();
		Element ade = new Element("Placemark");
		ade.addContent(XMLUtils.createElement("description", "Departed from " + aD.getName()));
		ade.addContent(XMLUtils.createElement("name", aD.getName() + " (" + aD.getICAO() + ")"));
		ade.addContent(XMLUtils.createElement("visibility", "1"));
		Element ads = new Element("Style");
		ads.addContent(addIcon(2, 0, 1)); // airplane icon
		ade.addContent(ads);
		Element adp = new Element("Point");
		adp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(aD)));
		ade.addContent(adp);
		ade.addContent(addLookAt(aD, 480, -1));
		results.add(ade);

		// Format the route
		Element le = new Element("Placemark");
		le.addContent(XMLUtils.createElement("name", "Flight Route"));
		le.addContent(XMLUtils.createElement("description", String.valueOf(info.getRouteData().size()) + " Position Records"));
		le.addContent(XMLUtils.createElement("visibility", "1"));
		Element ls = new Element("Style");
		Element lce = XMLUtils.createElement("LineStyle", "color", routeColor.toString());
		lce.addContent(XMLUtils.createElement("width", "2"));
		ls.addContent(lce);
		ls.addContent(XMLUtils.createElement("PolyStyle", "color", routeColor.dim(3).toString()));
		le.addContent(ls);
		Element lse = new Element("LineString");
		lse.addContent(XMLUtils.createElement("extrude", "1"));
		lse.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
		lse.addContent(XMLUtils.createElement("tessellate", "1"));

		// Format all of the coordinates
		StringBuilder buf = new StringBuilder();
		for (Iterator<RouteEntry> i = info.getRouteData().iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			buf.append(GeoUtils.format3D(entry, entry.getRadarAltitude()));
			buf.append(" \n");
		}

		// Save the coordinates
		lse.addContent(XMLUtils.createElement("coordinates", buf.toString()));
		le.addContent(lse);
		results.add(le);

		// Generate the end airport placemark
		Airport aA = info.getAirportA();
		Element aae = new Element("Placemark");
		aae.addContent(XMLUtils.createElement("description", "Landed at " + aA.getName()));
		aae.addContent(XMLUtils.createElement("name", aA.getName() + " (" + aA.getICAO() + ")"));
		aae.addContent(XMLUtils.createElement("visibility", "1"));
		Element aas = new Element("Style");
		aas.addContent(addIcon(2, 0, 1)); // airplane icon
		aae.addContent(aas);
		Element aap = new Element("Point");
		aap.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(aA)));
		aae.addContent(aap);
		aae.addContent(addLookAt(aA, 1500, -1));
		results.add(aae);

		// Format the positions
		if (showPositionData) {
			Element fe = new Element("Folder");
			fe.addContent(XMLUtils.createElement("name", "Position Records"));
			fe.addContent(XMLUtils.createElement("visibility", "0"));
			results.add(fe);
			int pos = 0;
			for (Iterator<RouteEntry> i = info.getRouteData().iterator(); i.hasNext();) {
				RouteEntry entry = i.next();
				Element pe = new Element("Placemark");
				pe.addContent(XMLUtils.createElement("name", "Route Point #" + String.valueOf(++pos)));
				pe.addContent(XMLUtils.createElement("description", entry.getInfoBox(), true));
				pe.addContent(XMLUtils.createElement("visibility", "0"));
				Element ps = new Element("Style");
				Element pis = addIcon(2, 0, 0); // info icon
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
				pe.addContent(addLookAt(entry, entry.getAltitude(), entry.getHeading()));
				fe.addContent(pe);
			}
		}
		
		// Format the flight plan data
		if (info.hasPlanData()) {
			Element fe = new Element("Folder");
			fe.addContent(XMLUtils.createElement("name", "Flight Plan"));
			fe.addContent(XMLUtils.createElement("visibility", "0"));
			results.add(fe);
			
			// Create route line
			Element rle = new Element("Placemark");
			rle.addContent(XMLUtils.createElement("name", "Flight Route"));
			rle.addContent(XMLUtils.createElement("description", info.getRoute()));
			rle.addContent(XMLUtils.createElement("visibility", "1"));
			Element rls = new Element("Style");
			Element rlce = XMLUtils.createElement("LineStyle", "color", GoogleEarthColor.make(224, 192, 192, 48).toString());
			rlce.addContent(XMLUtils.createElement("width", "2"));
			rls.addContent(rlce);
			rle.addContent(rls);
			Element rlse = new Element("LineString");
			rlse.addContent(XMLUtils.createElement("tessellate", "1"));

			// Loop through the points
			Random rnd = new Random();
			StringBuilder pbuf = new StringBuilder();
			for (Iterator<NavigationDataBean> i = info.getPlanData().iterator(); i.hasNext(); ) {
				NavigationDataBean wp = i.next();
				Element pe = new Element("Placemark");
				pe.addContent(XMLUtils.createElement("name", wp.getCode()));
				pe.addContent(XMLUtils.createElement("description", wp.getInfoBox(), true));
				pe.addContent(XMLUtils.createElement("visibility", "0"));
				Element pp = new Element("Point");
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(wp, 10)));
				pp.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
				pe.addContent(pp);
				pe.addContent(addLookAt(wp, 4500, rnd.nextInt(360)));
				
				// Format route point
				pbuf.append(GeoUtils.format3D(wp, 0));
				pbuf.append(" \n");

				// Add icon
				Element ps = new Element("Style");
				Element pis = null;
				switch (wp.getType()) {
					case NDB:
					case VOR:
						pis = addIcon(4, 0, 1);
						pis.addContent(XMLUtils.createElement("scale", "0.60"));
						ps.addContent(pis);
						pe.addContent(ps);
						break;
						
					case INT:
						pis = addIcon(4, 4, 0); // info icon
						pis.addContent(XMLUtils.createElement("scale", "0.50"));
						ps.addContent(pis);
						pe.addContent(ps);
						break;
				}
 
				if (pis != null)
					fe.addContent(pe);
			}
			
			// Save the coordinates
			rlse.addContent(XMLUtils.createElement("coordinates", pbuf.toString()));
			rle.addContent(rlse);
			fe.addContent(rle);
		}
		
		// Return results
		return results;
	}
}