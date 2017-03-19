// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

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
 * @version 7.3
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
		
		// Parse the data
		JSONObject req = null;
		try {
			req = new JSONObject(new JSONTokener(ctx.getRequest().getInputStream()));
		} catch (Exception e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		}
		
		// Check if we download runways
		boolean doRunways = req.optBoolean("runways");
		Simulator sim = Simulator.fromName(req.optString("simVersion"), Simulator.FSX);

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<Runway> runways = new LinkedHashSet<Runway>();
		Collection<WeatherDataBean> wxs = new ArrayList<WeatherDataBean>();
		List<Airport> alternates = new ArrayList<Airport>();
		Collection<Gate> gates = new TreeSet<Gate>(new GateComparator(GateComparator.USAGE)); 
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		
		// Get the departure/arrival airports
		DispatchRoute dr = new DispatchRoute();
		dr.setAirline(SystemData.getAirline(req.optString("airline")));
		dr.setAirportD(SystemData.getAirport(req.optString("airportD")));
		dr.setAirportA(SystemData.getAirport(req.optString("airportA")));
		dr.setAirportL(SystemData.getAirport(req.optString("airportL")));
		boolean isIntl = (dr.getAirportD() != null) && (dr.getAirportA() != null) && (!dr.getAirportD().getCountry().equals(dr.getAirportA().getCountry()));

		Aircraft a = null;
		try {
			Connection con = ctx.getConnection();
			GetAircraft acdao = new GetAircraft(con);
			GetNavRoute dao = new GetNavRoute(con);
			GetGates gdao = new GetGates(con);
			GetWeather wxdao = new GetWeather(con);
			
			String route = req.optString("route", "");
			
			// Load Aircraft
			a = acdao.get(req.optString("eqType"));
			
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
				
				String rwy = req.optString("runway", "");
				if (rwy.indexOf(' ') > 0)
					rwy = rwy.substring(rwy.indexOf(' ') + 1);
				
				final String rwyD = rwy;
				Collection<TerminalRoute> sids = new TreeSet<TerminalRoute>(dao.getRoutes(dr.getAirportD(), TerminalRoute.Type.SID));
				if (!StringUtils.isEmpty(rwyD))
					sids = sids.stream().filter(sid -> sid.getRunway().equals("ALL") || sid.getRunway().equals(rwyD)).collect(Collectors.toCollection(TreeSet::new));
				
				tRoutes.addAll(sids);
				
				// Get the departure gate
				Gate gateD = gdao.getGate(dr.getAirportD(), sim, req.optString("gateD"));
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
			TerminalRoute sid = dao.getRoute(dr.getAirportD(), TerminalRoute.Type.SID, req.optString("sid"));
			if (sid != null) {
				Runway r = dao.getRunway(dr.getAirportD(), sid.getRunway(), sim);
				if (r != null)
					routePoints.add(r);
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(sid.getWaypoints(wps.get(0)));
				else
					routePoints.addAll(sid.getWaypoints());
			} else if (dr.getAirportD() != null) {
				Runway r = dao.getRunway(dr.getAirportD(), req.optString("runway"), sim);
				if (r != null)
					routePoints.add(r);
			}

			// Add the route waypoints
			if (!StringUtils.isEmpty(route)) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(route, dr.getAirportD());
				routePoints.addAll(points);
			}

			// Check if we have a STAR
			TerminalRoute star = dao.getRoute(dr.getAirportA(), TerminalRoute.Type.STAR, req.optString("star"));
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

		// Convert points to a JSON object
		JSONObject jo = formatPoints(points, true);
		jo.put("intl", isIntl);
		if (dr.getAirline() != null) {
			JSONObject alo = new JSONObject();
			alo.put("name", dr.getAirline().getName());
			alo.put("code", dr.getAirline().getCode());
			jo.put("airline", alo);
		}
		
		if (dr.getAirportD() != null)
			jo.put("airportD", JSONUtils.format(dr.getAirportD()));
		if (dr.getAirportA() != null)
			jo.put("airportA", JSONUtils.format(dr.getAirportA()));
		
		// Add ETOPS rating
		JSONObject eo = new JSONObject();
		jo.put("etops", eo);
		eo.put("rating", er.getResult().toString());
		if (ETOPSHelper.validate(a, er.getResult())) {
			ETOPS erng = (a != null) && (a.getEngines() == 3) ? ETOPS.ETOPS120 : ETOPS.ETOPS90;
			eo.put("range", erng.getRange());
			eo.put("warning", true);
			eo.put("aircraftRating", erng.toString());
			for (NavigationDataBean ap : er.getClosestAirports()) {
				JSONObject apo = new JSONObject();
				apo.put("ll", JSONUtils.format(ap));
				apo.put("pal", ap.getPaletteCode());
				apo.put("icon", ap.getIconCode());
				apo.put("color", ap.getIconColor());
				apo.put("info", ap.getInfoBox());
				eo.append("airports", apo);
			}
			
			NavigationDataBean wp = (NavigationDataBean) er.getWarningPoint();
			JSONObject wo = new JSONObject();
			wo.put("ll", JSONUtils.format(wp));
			wo.put("pal", wp.getPaletteCode());
			wo.put("icon", wp.getIconCode());
			wo.put("info", wp.getInfoBox());
			eo.put("warnPoint", wo);
			JSONUtils.ensureArrayPresent(eo, "airports");
		} else
			eo.put("warning", false);
		
		// Add gates to XML document
		for (Gate g : gates) {
			boolean isDeparture = (dr.getAirportD() != null) && (g.getCode().equals(dr.getAirportD().getICAO()));
			JSONObject go = new JSONObject();
			go.put("name", g.getName());
			go.put("ll", JSONUtils.format(g));
			go.put("pal", g.getPaletteCode());
			go.put("icon", g.getIconCode());
			go.put("airlines", g.getAirlines().stream().map(Airline::getCode).collect(Collectors.toSet()));
			go.put("isIntl", g.isInternational());
			go.put("useCount", g.getUseCount());
			jo.put("info", g.getInfoBox());
			jo.append(isDeparture ? "departureGates" : "arrivalGates", go);
		}
		
		// Add SID/STAR names to XML document
		for (TerminalRoute tr : tRoutes) {
			JSONObject tro = new JSONObject();
			tro.put("name", tr.getName());
			tro.put("transition", tr.getTransition());
			tro.put("label", tr.getCode());
			tro.put("code", tr.toString().endsWith(".ALL") ? tr.getCode() + ".ALL" : tr.getCode());
			jo.append(tr.getType().name().toLowerCase(), tro);
		}
		
		// Add runways
		for (Runway r : runways) {
			if  ((a != null) && (a.getTakeoffRunwayLength() > r.getLength())) continue;
			JSONObject ro = new JSONObject();
			ro.put("code", r.getComboAlias());
			
			// Build the label
			StringBuilder buf = new StringBuilder("Runway ");
			buf.append(r.getName());
			buf.append(" (");
			buf.append(r.getLength());
			buf.append(" feet - ");
			buf.append(r.getHeading());
			buf.append(" degrees)");
			ro.put("label", buf.toString());
			jo.append("runways", ro);
		}
		
		// Add weather
		for (WeatherDataBean wx : wxs) {
			JSONObject wo = new JSONObject();
			wo.put("type", wx.getType().name().toLowerCase());
			wo.put("icao", wx.getCode());
			wo.put("info", wx.getData());
			wo.put("date", wx.getDate().toEpochMilli());
			wo.put("dst", (dr.getAirportD() == null) || !wx.getCode().equals(dr.getAirportD().getICAO()));
			jo.append("wx", wo);
		}
		
		// Add alternates
		jo.put("alternates", new JSONArray()); // ensure present
		for (Airport alt : alternates) {
			JSONObject ao = new JSONObject();
			ao.put("iata", alt.getIATA());
			ao.put("icao", alt.getICAO());
			ao.put("name", alt.getName());
			jo.append("alternates", ao);
		}
		
		// Ensure arrays are populated
		JSONUtils.ensureArrayPresent(jo, "departureGates", "arrivalGates", "runways", "sid", "star", "wx", "alternates");
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString(1));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}
}