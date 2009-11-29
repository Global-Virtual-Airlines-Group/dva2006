// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.wx.*;

import org.deltava.comparators.RunwayComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display plotted flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class RoutePlotMapService extends MapPlotService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we download runways
		boolean doRunways = Boolean.valueOf(ctx.getParameter("runways")).booleanValue();

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<Runway> runways = new LinkedHashSet<Runway>();
		Collection<METAR> wx = new ArrayList<METAR>();
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			GetACARSRunways acdao = new GetACARSRunways(con);
			GetWeather wxdao = new GetWeather(con);
			
			// Translate IATA to ICAO codes
			String airportDCode = txIATA(ctx.getParameter("airportD"));
			String airportACode = txIATA(ctx.getParameter("airportA"));
			String airportLCode = txIATA(ctx.getParameter("airportL"));
			
			// Get the departure/arrival airports
			AirportLocation aD = dao.getAirport(airportDCode);
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
					Collection<Runway> popRunways = acdao.getPopularRunways(SystemData.getAirport(airportDCode), SystemData.getAirport(airportACode), true);
					Collection<String> sidRunways = dao.getSIDRunways(aD.getCode());
					for (Runway r : popRunways) {
						String code = "RW" + r.getName();
						if (sidRunways.contains(code))
							runways.add(r);
					}
					
					// Sort runways based on wind heading
					if (wxD != null) {
						RunwayComparator rcmp = new RunwayComparator(wxD.getWindDirection());
						runways = CollectionUtils.sort(runways, Collections.reverseOrder(rcmp));
					}
				}
			}

			// Check if we have a SID
			List<String> wps = StringUtils.split(route, " ");
			TerminalRoute sid = dao.getRoute(ctx.getParameter("sid"));
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

			// Check if we have a STAR
			TerminalRoute star = dao.getRoute(ctx.getParameter("star"));
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
		List<NavigationDataBean> points = new ArrayList<NavigationDataBean>(routePoints);

		// Convert points to an XML document
		Document doc = formatPoints(points, true);
		Element re = doc.getRootElement();

		// Add SID/STAR names to XML document
		for (Iterator<TerminalRoute> i = tRoutes.iterator(); i.hasNext();) {
			TerminalRoute tr = i.next();
			Element e = new Element(tr.getTypeName().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("label", tr.getCode());
			e.setAttribute("code", tr.toString().endsWith(".ALL") ? tr.getCode() + ".ALL" : tr.getCode());
			re.addContent(e);
		}
		
		// Add runways
		for (Iterator<Runway> i = runways.iterator(); i.hasNext(); ) {
			Runway r = i.next();
			Element e = new Element("runway");
			e.setAttribute("code", "RW" + r.getName());
			
			// Build the labe
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
		for (Iterator<METAR> i = wx.iterator(); i.hasNext(); ) {
			METAR m = i.next();
			Element e = XMLUtils.createElement("data", m.getData(), true);
			//e.setAttribute("code", m.getCode());
			e.setAttribute("wHdg", String.valueOf(m.getWindDirection()));
			e.setAttribute("wSpeed", String.valueOf(m.getWindSpeed()));
			e.setAttribute("wGust", String.valueOf(m.getWindGust()));
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}