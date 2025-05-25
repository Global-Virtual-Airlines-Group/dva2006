// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2015, 2016, 2017, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RunwayUsage;
import org.deltava.beans.wx.*;

import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display plotted flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 12.0
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
		} catch (IOException ie) {
			throw error(SC_BAD_REQUEST, ie.getMessage(), false);
		} catch (Exception e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		}
		
		// Check if we download runways
		boolean doRunways = req.optBoolean("runways");
		boolean allDepartureRunways = req.optBoolean("allSID");

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<Runway> runways = new LinkedHashSet<Runway>();
		Collection<WeatherDataBean> wxs = new ArrayList<WeatherDataBean>();
		List<Airport> alternates = new ArrayList<Airport>();
		Collection<Gate> gates = new TreeSet<Gate>(new GateComparator(GateComparator.USAGE).reversed()); 
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		
		// Get the departure/arrival airports
		DispatchRoute dr = new DispatchRoute();
		dr.setAirline(SystemData.getAirline(req.optString("airline")));
		dr.setAirportD(SystemData.getAirport(req.optString("airportD")));
		dr.setAirportA(SystemData.getAirport(req.optString("airportA")));
		dr.setAirportL(SystemData.getAirport(req.optString("airportL")));
		
		Aircraft a = null;
		try {
			Connection con = ctx.getConnection();
			GetAircraft acdao = new GetAircraft(con);
			GetNavRoute dao = new GetNavRoute(con);
			GetGates gdao = new GetGates(con);
			GetWeather wxdao = new GetWeather(con);
			
			// Load Aircraft and route
			a = acdao.get(req.optString("eqType"));
			String route = req.optString("route", "");
			
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
			GateHelper gh = new GateHelper(dr, dr.getAirline(), req.optBoolean("allGates") ? Integer.MAX_VALUE : 8, false);
			List<String> wps = StringUtils.split(route, " ");
			if (dr.getAirportD() != null) {
				gh.addDepartureGates(gdao.getGates(dr.getAirportD()), gdao.getUsage(dr, true, ctx.getDB()));
				gates.addAll(gh.getDepartureGates());
				String rwy = req.optString("runway", "");
				if (rwy.indexOf(' ') > 0)
					rwy = rwy.substring(rwy.indexOf(' ') + 1);
				
				// Get the departure gate
				Gate gateD = gdao.getGate(dr.getAirportD(), req.optString("gateD"));
				if (gateD != null)
					routePoints.add(gateD);
				else
					routePoints.add(new AirportLocation(dr.getAirportD()));
				
				Collection<TerminalRoute> sids = new TreeSet<TerminalRoute>(dao.getRoutes(dr.getAirportD(), TerminalRoute.Type.SID));
				if (!StringUtils.isEmpty(rwy)) {
					Runway rD = dao.getRunway(dr.getAirportD(), rwy, Simulator.P3Dv4);
					if (rD != null) {
						sids = sids.stream().filter(sid -> rD.matches(sid.getRunway())).collect(Collectors.toCollection(TreeSet::new));
						rwy = String.format("RW%s", rD.isAltNew() ? rD.getAlternateCode() : rD.getName());
					}
				}
				
				tRoutes.addAll(sids);
				
				// Check if we have a SID, update if the sid is no longer applicable for the selected departure runway
				TerminalRoute sid = dao.getRoute(dr.getAirportD(), TerminalRoute.Type.SID, req.optString("sid"));
				if ((sid != null) && !sid.getRunway().equals(rwy))
					sid = dao.getBestRoute(dr.getAirportD(), TerminalRoute.Type.SID, sid.getName(), sid.getTransition(), rwy);
				
				if (sid != null) {
					req.put("sid", sid.toString());
					Runway r = dao.getRunway(dr.getAirportD(), sid.getRunway(), Simulator.P3Dv4);
					if (r != null)
						routePoints.add(r);
					if (!CollectionUtils.isEmpty(wps))
						routePoints.addAll(sid.getWaypoints(wps.get(0)));
					else
						routePoints.addAll(sid.getWaypoints());
				} else if (dr.getAirportD() != null) {
					Runway r = dao.getRunway(dr.getAirportD(), req.optString("runway"), Simulator.P3Dv4);
					if (r != null)
						routePoints.add(r);
				}
				
				// Add popular departure runways
				if (doRunways) {
					UsageFilter<RunwayUse> rf = UsagePercentFilter.ALL;
					if (!allDepartureRunways) {
						UsageWindFilter uwf = new UsageWindFilter(10, -7);
						uwf.setWinds(wxD);
						rf = uwf;
					}
					
					GetRunwayUsage rwdao = new GetRunwayUsage(con);
					GetRunwayMapping rwmdao = new GetRunwayMapping(con);
					RunwayUsage dru = rwdao.getPopularRunways(dr, true);
					Collection<RunwayMapping> rwmaps = rwmdao.getAll(dr.getAirportD());
					rwmaps.forEach(dru::apply);
					List<Runway> depRwys = dao.getRunways(dr.getAirportD(), Simulator.P3Dv4);
					Collection<RunwayUse> popRunways = rf.filter(dru.apply(depRwys));
					if (popRunways.isEmpty()) {
						dru = rwdao.getPopularRunways(dr.getAirportD(), true);
						popRunways.addAll(rf.filter(dru.apply(depRwys)));
					}
					
					final Collection<String> sidRunways = dao.getSIDRunways(dr.getAirportD());
					popRunways.stream().filter(r -> filterSIDRwy(r, sidRunways)).forEach(runways::add);
					
					// Sort runways based on wind heading
					if ((wxD != null) && (wxD.getWindSpeed() > 0))
						runways = CollectionUtils.sort(runways, new RunwayComparator(wxD.getWindDirection(), wxD.getWindSpeed(), true));
				}
			}

			// Add the route waypoints
			if (!StringUtils.isEmpty(route)) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(route, dr.getAirportD());
				routePoints.addAll(points);
			}

			// Check if we have a STAR
			TerminalRoute star = dao.getRoute(dr.getAirportA(), TerminalRoute.Type.STAR, req.optString("star"));
			if (star != null) {
				req.put("star", star.toString());
				if (!CollectionUtils.isEmpty(wps))
					routePoints.addAll(star.getWaypoints(wps.getLast()));
				else
					routePoints.addAll(star.getWaypoints());
				
				Runway r = dao.getRunway(dr.getAirportA(), star.getRunway(), Simulator.P3Dv4);
				if (r != null)
					routePoints.add(r);
			}

			// Add the arrival airport
			if (dr.getAirportA() != null) {
				routePoints.add(new AirportLocation(dr.getAirportA()));
				gh.addArrivalGates(gdao.getGates(dr.getAirportA()), gdao.getUsage(dr, false, ctx.getDB()));
				gates.addAll(gh.getArrivalGates());
				Set<TerminalRoute> stars = new TreeSet<TerminalRoute>(dao.getRoutes(dr.getAirportA(), TerminalRoute.Type.STAR));
				tRoutes.addAll(stars);
				
				if (a != null) {
					AlternateAirportHelper hlp = new AlternateAirportHelper(SystemData.get("airline.code"));
					alternates.addAll(hlp.calculateAlternates(a, dr.getAirportA()));
					if (alternates.size() > 10)
						alternates.subList(10, alternates.size()).clear();
				}
				
				// If the selected alternate isn't in the list, clear it
				if (dr.getAirportL() != null) {
					boolean noAlt = true;
					for (Airport ap : alternates) {
						if (ap.getICAO().equals(dr.getAirportL().getICAO())) {
							noAlt = false;
							break;
						}
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
		JSONObject jo = formatPoints(points);
		jo.put("flightType", dr.getFlightType().name());
		if (dr.getAirline() != null)
			jo.put("airline", JSONUtils.format(dr.getAirline()));
		
		if (dr.getAirportD() != null)
			jo.put("airportD", JSONUtils.format(dr.getAirportD()));
		if (dr.getAirportA() != null)
			jo.put("airportA", JSONUtils.format(dr.getAirportA()));
		if (dr.isPopulated()) {
			DistanceUnit du = ctx.isAuthenticated() ? ctx.getUser().getDistanceType() : DistanceUnit.MI;
			JSONObject duo = new JSONObject();
			duo.put("id", du.name());
			duo.put("name", du.getUnitName());
			duo.put("factor", du.getFactor());
			jo.put("distanceUnit", duo);
		}
		
		// Add ETOPS rating
		JSONObject eo = new JSONObject();
		jo.put("etops", eo);
		eo.put("rating", er.getResult().toString());
		eo.put("time", er.getResult().getTime());
		AircraftPolicyOptions opts = (a == null) ? null : a.getOptions(SystemData.get("airline.code"));
		ETOPS ae = (opts == null) ? ETOPS.ETOPS90 : opts.getETOPS();
		if (req.optBoolean("etopsCheck", true) && ETOPSHelper.isWarn(ae, er.getResult())) {
			eo.put("range", ae.getRange());
			eo.put("warning", true);
			eo.put("aircraftRating", ae.toString());
			if (er.getWarningPoint() != null) {
				NavigationDataBean wp = er.getWarningPoint();
				JSONObject wo = new JSONObject();
				wo.put("ll", JSONUtils.format(wp));
				wo.put("pal", wp.getPaletteCode());
				wo.put("icon", wp.getIconCode());
				wo.put("info", wp.getInfoBox());
				eo.put("warnPoint", wo);
			}
			
			for (NavigationDataBean ap : er.getClosestAirports()) {
				JSONObject apo = new JSONObject();
				apo.put("ll", JSONUtils.format(ap));
				apo.put("icao", ap.getCode());
				apo.put("pal", ap.getPaletteCode());
				apo.put("icon", ap.getIconCode());
				apo.put("color", ap.getIconColor());
				apo.put("info", ap.getInfoBox());
				eo.append("airports", apo);
			}
			
			JSONUtils.ensureArrayPresent(eo, "airports");
		} else
			eo.put("warning", false);
		
		// Check for restricted Airspace
		Collection<Airspace> rsts = AirspaceHelper.classify(dr, true);
		for (Airspace r : rsts) {
			JSONObject ao = new JSONObject();
			ao.put("id", r.getID());
			ao.put("type", r.getType().name());
			ao.put("min", r.getMinAltitude());
			ao.put("max", r.getMaxAltitude());
			ao.put("exclude", r.isExclusion());
			ao.put("info", r.getInfoBox());
			ao.put("ll", JSONUtils.format(r));
			r.getBorder().forEach(pt -> ao.append("border", JSONUtils.format(pt)));
			jo.append("airspace", ao);
		}
		
		// Add gates to XML document
		for (Gate g : gates) {
			boolean isDeparture = (dr.getAirportD() != null) && (g.getCode().equals(dr.getAirportD().getICAO()));
			JSONObject go = new JSONObject();
			go.put("name", g.getName());
			go.put("ll", JSONUtils.format(g));
			go.put("pal", g.getPaletteCode());
			go.put("icon", g.getIconCode());
			go.put("airlines", g.getAirlines().stream().map(Airline::getCode).collect(Collectors.toSet()));
			go.put("zone", g.getZone().getDescription());
			go.put("zoneCode", g.getZone().ordinal());
			go.put("useCount", g.getUseCount());
			go.put("info", g.getInfoBox());
			jo.append(isDeparture ? "departureGates" : "arrivalGates", go);
		}
		
		// Add SID/STAR names to XML document
		for (TerminalRoute tr : tRoutes) {
			JSONObject tro = new JSONObject();
			tro.put("name", tr.getName());
			tro.put("transition", tr.getTransition());
			tro.put("label", tr.getCode());
			tro.put("code", tr.toString().endsWith(".ALL") ? tr.getCode() + ".ALL" : tr.getCode());
			tro.put("isSelected", tr.toString().equals(req.optString(tr.getType().name().toLowerCase(), "")));
			jo.append(tr.getType().name().toLowerCase(), tro);
		}
		
		// Add runways
		for (Runway r : runways) {
			if  ((opts != null) && (opts.getTakeoffRunwayLength() > r.getLength())) continue;
			JSONObject ro = new JSONObject();
			ro.put("code", r.getComboAlias());
			ro.put("useCount", (r instanceof UseCount uc) ? uc.getUseCount() : 0);
			
			// Build the label
			StringBuilder buf = new StringBuilder("Runway ");
			buf.append(r.getName());
			buf.append(" (");
			buf.append(r.getLength());
			buf.append(" feet - ");
			buf.append(r.getHeading());
			buf.append(" degrees)");
			ro.put("label", ctx.isUserInRole("Developer") || ctx.isUserInRole("Operations") ? r.getComboName() : buf.toString());
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
			if (wx.getType() == WeatherDataBean.Type.METAR) {
				METAR m = (METAR) wx;
				wo.put("windSpeed", m.getWindSpeed());
				wo.put("windGust", m.getWindGust());
				wo.put("windDirection", m.getWindDirection());
			}
			
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
		
		// Dump the XML to the output stream
		JSONUtils.ensureArrayPresent(jo, "departureGates", "arrivalGates", "runways", "sid", "star", "wx", "alternates", "airspace");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString(1));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}
	
	/*
	 * Helper method to filter SID runways using current and optional old runway codes.
	 */
	private static boolean filterSIDRwy(Runway r, Collection<String> rwyCodes) {
		return rwyCodes.contains("RW" + r.getName()) || ((r.getAlternateCode() != null) && rwyCodes.contains("RW" + r.getAlternateCode()));
	}
}