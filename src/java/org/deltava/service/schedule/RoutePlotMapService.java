// Copyright 2005, 2006, 2007, 2008, 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.*;

import org.deltava.comparators.RunwayComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display plotted flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class RoutePlotMapService extends MapPlotService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we download runways
		boolean doRunways = Boolean.valueOf(ctx.getParameter("runways")).booleanValue();

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<Runway> runways = new LinkedHashSet<Runway>();
		Collection<METAR> wx = new ArrayList<METAR>();
		List<Airport> alternates = new ArrayList<Airport>();
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		AirportLocation aD = null;
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			GetWeather wxdao = new GetWeather(con);
			
			// Translate IATA to ICAO codes
			String airportDCode = txIATA(ctx.getParameter("airportD"));
			String airportACode = txIATA(ctx.getParameter("airportA"));
			String airportLCode = txIATA(ctx.getParameter("airportL"));
			
			// Get the departure/arrival airports
			aD = dao.getAirport(airportDCode);
			AirportLocation aA = dao.getAirport(airportACode);
			AirportLocation aL = dao.getAirport(airportLCode);
			String route = ctx.getParameter("route");
			
			// Get the weather
			METAR wxD = wxdao.getMETAR(airportDCode);
			if (wxD != null)
				wx.add(wxD);
			METAR wxA = wxdao.getMETAR(airportACode);
			if (wxA != null)
				wx.add(wxA);

			// Add the departure airport
			if (aD != null) {
				routePoints.add(aD);
				Set<TerminalRoute> sids = new TreeSet<TerminalRoute>(dao.getRoutes(aD.getCode(), TerminalRoute.SID));
				tRoutes.addAll(sids);
				
				// Add popular departure runways
				if (doRunways) {
					GetACARSRunways rwdao = new GetACARSRunways(con);
					Collection<Runway> popRunways = rwdao.getPopularRunways(SystemData.getAirport(airportDCode), SystemData.getAirport(airportACode), true);
					if (popRunways.isEmpty())
						popRunways.addAll(rwdao.getPopularRunways(SystemData.getAirport(airportDCode), null, true));
					
					Collection<String> sidRunways = dao.getSIDRunways(aD.getCode());
					for (Runway r : popRunways) {
						String code = "RW" + r.getName();
						if (sidRunways.contains(code))
							runways.add(r);
					}
					
					// Sort runways based on wind heading
					if ((wxD != null) && (wxD.getWindSpeed() > 0))
						runways = CollectionUtils.sort(runways, new RunwayComparator(wxD.getWindDirection()).reverse());
				}
			}

			// Check if we have a SID
			List<String> wps = StringUtils.split(route, " ");
			TerminalRoute sid = dao.getRoute(aD, TerminalRoute.SID, ctx.getParameter("sid"));
			if (sid != null) {
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(sid.getWaypoints(wps.get(0)));
				else
					routePoints.addAll(sid.getWaypoints());
			}

			// Add the route waypoints
			if (!StringUtils.isEmpty(route)) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(route, aD);
				routePoints.addAll(points);
			}

			// Check if we have a STAR - ensure aD is passed in
			TerminalRoute star = dao.getRoute(aA, TerminalRoute.STAR, ctx.getParameter("star"));
			if (star != null) {
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(star.getWaypoints(wps.get(wps.size() - 1)));
				else
					routePoints.addAll(star.getWaypoints());
			}

			// Add the arrival airport
			if (aA != null) {
				routePoints.add(aA);
				Set<TerminalRoute> stars = new TreeSet<TerminalRoute>(dao.getRoutes(aA.getCode(), TerminalRoute.STAR));
				tRoutes.addAll(stars);
				
				// Calculate alternates
				GetAircraft acdao = new GetAircraft(con);
				Aircraft a = acdao.get(ctx.getParameter("eqType"));
				if (a != null) {
					alternates.addAll(AlternateAirportHelper.calculateAlternates(a, aA));
					if (alternates.size() > 10)
						alternates.subList(10, alternates.size()).clear();
				}
				
				// If the selected alternate isn't in the list, clear it
				if (aL != null) {
					boolean noAlt = true;
					for (Iterator<Airport> i = alternates.iterator(); noAlt && i.hasNext(); ) {
						Airport ap = i.next();
						if (ap.getICAO().equals(aL.getICAO()))
							noAlt = false;
					}
				
					if (noAlt)
						aL = null;
				}
			}
			
			// Add the alternate
			if (aL != null)
				routePoints.add(aL);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Convert the points into a List
		List<NavigationDataBean> points = GeoUtils.stripDetours(routePoints, 60);

		// Convert points to an XML document
		Document doc = formatPoints(points, true);
		Element re = doc.getRootElement();

		// Add SID/STAR names to XML document
		for (TerminalRoute tr : tRoutes) {
			Element e = new Element(tr.getTypeName().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("label", tr.getCode());
			e.setAttribute("code", tr.toString().endsWith(".ALL") ? tr.getCode() + ".ALL" : tr.getCode());
			re.addContent(e);
		}
		
		// Add runways
		for (Runway r : runways) {
			Element e = new Element("runway");
			e.setAttribute("code", "RW" + r.getName());
			
			// Build the label
			StringBuilder buf = new StringBuilder("Runway ");
			buf.append(r.getName());
			buf.append(" (");
			buf.append(r.getLength());
			buf.append(" feet - ");
			buf.append(r.getHeading());
			buf.append(" degrees)");
			e.setAttribute("label", buf.toString());
			re.addContent(e);
		}
		
		// Add weather
		for (METAR m : wx) {
			Element e = XMLUtils.createElement("wx", m.getData(), true);
			e.setAttribute("icao", m.getCode());
			e.setAttribute("dst", String.valueOf(!m.getCode().equals(aD.getCode())));
			re.addContent(e);
		}
		
		// Add alternates
		for (Airport alt : alternates) {
			Element e = new Element("alt");
			e.setAttribute("iata", alt.getIATA());
			e.setAttribute("icao", alt.getICAO());
			e.setAttribute("name", alt.getName());
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}