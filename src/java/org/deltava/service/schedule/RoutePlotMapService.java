// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.*;

import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display plotted flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 6.4
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
		Simulator sim = Simulator.fromName(ctx.getParameter("simVersion"), Simulator.FSX);

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<Runway> runways = new LinkedHashSet<Runway>();
		Collection<WeatherDataBean> wxs = new ArrayList<WeatherDataBean>();
		List<Airport> alternates = new ArrayList<Airport>();
		Collection<Gate> gates = new TreeSet<Gate>(new GateComparator(GateComparator.USAGE)); 
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		
		// Get the departure/arrival airports
		DispatchRoute dr = new DispatchRoute();
		dr.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
		dr.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		dr.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		dr.setAirportL(SystemData.getAirport(ctx.getParameter("airportL")));
		boolean isIntl = (dr.getAirportD() != null) && (dr.getAirportA() != null) && (!dr.getAirportD().getCountry().equals(dr.getAirportA().getCountry()));

		Aircraft a = null;
		try {
			Connection con = ctx.getConnection();
			GetAircraft acdao = new GetAircraft(con);
			GetNavRoute dao = new GetNavRoute(con);
			GetGates gdao = new GetGates(con);
			GetWeather wxdao = new GetWeather(con);
			
			String route = ctx.getParameter("route");
			
			// Load Aircraft
			a = acdao.get(ctx.getParameter("eqType"));
			
			// Get the weather
			METAR wxD = wxdao.getMETAR(dr.getAirportD());
			if (wxD != null)
				wxs.add(wxD);
			if (dr.getAirportA() != null) {
				WeatherDataBean wx = wxdao.getMETAR(dr.getAirportA());
				if (wx != null)
					wxs.add(wx);
				wx = wxdao.getTAF(dr.getAirportA().getICAO());
				if (wx != null)
					wxs.add(wx);
			}

			// Add the departure airport
			if (dr.getAirportD() != null) {
				Collection<Gate> dGates = gdao.getPopularGates(dr, sim, true);
				if (dr.getAirline() != null) {
					List<Gate> fdGates = dGates.stream().filter(g -> g.getAirlines().contains(dr.getAirline())).collect(Collectors.toList());
					if (!fdGates.isEmpty()) {
						dGates = fdGates;
						if (isIntl)
							fdGates = fdGates.stream().filter(g -> g.isInternational()).collect(Collectors.toList());
						if (fdGates.isEmpty())
							dGates = fdGates;
					}
				}

				gates.addAll(dGates);
				if (dGates.size() < 3)
					gates.addAll(gdao.getGates(dr.getAirportD(), sim));
				
				String rwyD = ctx.getParameter("runway");
				Collection<TerminalRoute> sids = new TreeSet<TerminalRoute>(dao.getRoutes(dr.getAirportD(), TerminalRoute.Type.SID));
				if (!StringUtils.isEmpty(rwyD))
					sids = sids.stream().filter(sid -> sid.getRunway().equals("ALL") || sid.getRunway().equals(rwyD)).collect(Collectors.toCollection(TreeSet::new));
				
				tRoutes.addAll(sids);
				
				// Get the departure gate
				Gate gateD = gdao.getGate(dr.getAirportD(), sim, ctx.getParameter("gateD"));
				if (gateD != null)
					routePoints.add(gateD);
				else
					routePoints.add(new AirportLocation(dr.getAirportD()));
				
				// Add popular departure runways
				if (doRunways) {
					GetACARSRunways rwdao = new GetACARSRunways(con);
					Collection<Runway> popRunways = rwdao.getPopularRunways(dr.getAirportD(), dr.getAirportA(), true);
					if (popRunways.isEmpty())
						popRunways.addAll(rwdao.getPopularRunways(dr.getAirportD(), null, true));
					
					Collection<String> sidRunways = dao.getSIDRunways(dr.getAirportD());
					for (Runway r : popRunways) {
						String code = "RW" + r.getName();
						if (sidRunways.contains(code))
							runways.add(r);
					}
					
					// Sort runways based on wind heading
					if ((wxD != null) && (wxD.getWindSpeed() > 0))
						runways = CollectionUtils.sort(runways, new RunwayComparator(wxD.getWindDirection(), wxD.getWindSpeed()));
				}
			}

			// Check if we have a SID
			List<String> wps = StringUtils.split(route, " ");
			TerminalRoute sid = dao.getRoute(dr.getAirportD(), TerminalRoute.Type.SID, ctx.getParameter("sid"));
			if (sid != null) {
				Runway r = dao.getRunway(dr.getAirportD(), sid.getRunway(), sim);
				if (r != null)
					routePoints.add(r);
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(sid.getWaypoints(wps.get(0)));
				else
					routePoints.addAll(sid.getWaypoints());
			} else if (dr.getAirportD() != null) {
				Runway r = dao.getRunway(dr.getAirportD(), ctx.getParameter("runway"), sim);
				if (r != null)
					routePoints.add(r);
			}

			// Add the route waypoints
			if (!StringUtils.isEmpty(route)) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(route, dr.getAirportD());
				routePoints.addAll(points);
			}

			// Check if we have a STAR
			TerminalRoute star = dao.getRoute(dr.getAirportA(), TerminalRoute.Type.STAR, ctx.getParameter("star"));
			if (star != null) {
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(star.getWaypoints(wps.get(wps.size() - 1)));
				else
					routePoints.addAll(star.getWaypoints());
				
				Runway r = dao.getRunway(dr.getAirportA(), star.getRunway(), sim);
				if (r != null)
					routePoints.add(r);
			}

			// Add the arrival airport
			if (dr.getAirportA() != null) {
				routePoints.add(new AirportLocation(dr.getAirportA()));
				Collection<Gate> aGates = gdao.getPopularGates(dr, sim, false);
				if (dr.getAirline() != null) {
					List<Gate> adGates = aGates.stream().filter(g -> g.getAirlines().contains(dr.getAirline())).collect(Collectors.toList());
					if (!adGates.isEmpty()) {
						aGates = adGates;
						if (isIntl)
							adGates = adGates.stream().filter(g -> g.isInternational()).collect(Collectors.toList());
						if (!adGates.isEmpty())
							aGates= adGates;
					}
				}
				
				gates.addAll(aGates);
				if (aGates.size() < 3)
					gates.addAll(gdao.getGates(dr.getAirportA(), sim));
				
				Set<TerminalRoute> stars = new TreeSet<TerminalRoute>(dao.getRoutes(dr.getAirportA(), TerminalRoute.Type.STAR));
				tRoutes.addAll(stars);
				
				if (a != null) {
					alternates.addAll(AlternateAirportHelper.calculateAlternates(a, dr.getAirportA()));
					if (alternates.size() > 10)
						alternates.subList(10, alternates.size()).clear();
				}
				
				// If the selected alternate isn't in the list, clear it
				if (dr.getAirportL() != null) {
					boolean noAlt = true;
					for (Iterator<Airport> i = alternates.iterator(); noAlt && i.hasNext(); ) {
						Airport ap = i.next();
						if (ap.getICAO().equals(dr.getAirportL().getICAO()))
							noAlt = false;
					}
				
					if (noAlt)
						dr.setAirportL(null);
				}
			}
			
			// Add the alternate
			if (dr.getAirportL() != null)
				routePoints.add(new AirportLocation(dr.getAirportL()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Convert the points into a List and get ETOPS rating
		List<NavigationDataBean> points = GeoUtils.stripDetours(routePoints, 60);
		dr.addWaypoints(points);
		ETOPSResult er = ETOPSHelper.classify(dr);

		// Convert points to an XML document
		Document doc = formatPoints(points, true);
		Element re = doc.getRootElement();
		re.setAttribute("intl", String.valueOf(isIntl));
		if (dr.getAirline() != null) {
			Element ae = new Element("airline");
			ae.setAttribute("name", dr.getAirline().getName());
			ae.setAttribute("code", dr.getAirline().getCode());
			re.addContent(ae);
		}
		
		if (dr.getAirportD() != null) {
			Element ade = new Element("airportD");
			ade.setAttribute("lat", StringUtils.format(dr.getAirportD().getLatitude(), "#0.00000"));
			ade.setAttribute("lng", StringUtils.format(dr.getAirportD().getLongitude(), "##0.00000"));
			re.addContent(ade);
		}
		
		// Add ETOPS rating
		Element ee = new Element("etops");
		re.addContent(ee);
		ee.setAttribute("rating", er.getResult().toString());
		if (ETOPSHelper.validate(a, er.getResult())) {
			ETOPS erng = (a != null) && (a.getEngines() == 3) ? ETOPS.ETOPS120 : ETOPS.ETOPS90;
			ee.setAttribute("range", String.valueOf(erng.getRange()));
			ee.setAttribute("warning", "true");
			for (NavigationDataBean ap : er.getClosestAirports()) {
				Element eae = XMLUtils.createElement("airport", ap.getInfoBox(), true);
				eae.setAttribute("lat", StringUtils.format(ap.getLatitude(), "##0.00000"));
				eae.setAttribute("lng", StringUtils.format(ap.getLongitude(), "##0.00000"));
				eae.setAttribute("pal", String.valueOf(ap.getPaletteCode()));
				eae.setAttribute("icon", String.valueOf(ap.getIconCode()));
				ee.addContent(eae);
			}
			
			NavigationDataBean wp = (NavigationDataBean) er.getWarningPoint();
			Element eae = XMLUtils.createElement("warnPoint", wp.getInfoBox(), true);
			eae.setAttribute("lat", StringUtils.format(wp.getLatitude(), "##0.00000"));
			eae.setAttribute("lng", StringUtils.format(wp.getLongitude(), "##0.00000"));
			eae.setAttribute("pal", String.valueOf(wp.getPaletteCode()));
			eae.setAttribute("icon", String.valueOf(wp.getIconCode()));
			ee.addContent(eae);
		}
		
		// Add gates to XML document
		for (Gate g : gates) {
			boolean isDeparture = (dr.getAirportD() != null) && (g.getCode().equals(dr.getAirportD().getICAO()));
			Element e = XMLUtils.createElement(isDeparture ? "gateD" : "gateA", g.getInfoBox(), true) ;
			e.setAttribute("name", g.getName());
			e.setAttribute("lat", StringUtils.format(g.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(g.getLongitude(), "##0.00000"));
			e.setAttribute("pal", String.valueOf(g.getPaletteCode()));
			e.setAttribute("icon", String.valueOf(g.getIconCode()));
			Collection<String> alCodes = g.getAirlines().stream().map(Airline::getCode).collect(Collectors.toSet());
			e.setAttribute("airlines", StringUtils.listConcat(alCodes, ","));
			e.setAttribute("isIntl", String.valueOf(g.isInternational()));
			e.setAttribute("useCount", String.valueOf(g.getUseCount()));
			re.addContent(e);
		}
		
		// Add SID/STAR names to XML document
		for (TerminalRoute tr : tRoutes) {
			Element e = new Element(tr.getType().name().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("label", tr.getCode());
			e.setAttribute("code", tr.toString().endsWith(".ALL") ? tr.getCode() + ".ALL" : tr.getCode());
			re.addContent(e);
		}
		
		// Add runways
		for (Runway r : runways) {
			if  ((a != null) && (a.getTakeoffRunwayLength() > r.getLength())) continue;
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
		for (WeatherDataBean wx : wxs) {
			Element e = XMLUtils.createElement("wx", wx.getData(), true);
			e.setAttribute("type", wx.getType().name().toLowerCase());
			e.setAttribute("icao", wx.getCode());
			if (dr.getAirportD() != null)
				e.setAttribute("dst", String.valueOf(!wx.getCode().equals(dr.getAirportD().getICAO())));
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
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}
}