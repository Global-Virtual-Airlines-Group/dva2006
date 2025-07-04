// Copyright 2010, 2011, 2012, 2014, 2015, 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RunwayUsage;
import org.deltava.beans.wx.METAR;

import org.deltava.comparators.RunwayComparator;

import org.deltava.dao.http.GetFARoutes;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A utility class to load flight routes from the database.
 * @author Luke
 * @version 11.1
 * @since 3.4
 */

@Helper(FlightRoute.class)
public final class RouteLoadHelper {
	
	private static final Logger log = LogManager.getLogger(RouteLoadHelper.class);
	
	private transient final Connection _c;
	private final RoutePair _rp;
	private METAR _mD; private METAR _mA;
	private String _preferredRunway;
	
	private final Collection<FlightRoute> _routes = new ArrayList<FlightRoute>();
	
	/**
	 * Initializes the helper.
	 * @param c the JDBC connection to use
	 * @param rp the RoutePar
	 */
	public RouteLoadHelper(Connection c, RoutePair rp) {
		super();
		_c = c;
		_rp = rp;
	}
	
	/**
	 * Sets the preferred departure runway.
	 * @param rwy the runway ID, or null if none
	 */
	public void setPreferredRunway(String rwy) {
		_preferredRunway = rwy;
		if ((rwy != null) && !rwy.startsWith("RW"))
			_preferredRunway = null;
	}

	/**
	 * Returns the loaded Flight Routes.
	 * @return a Collection of FlightRoute beans
	 */
	public Collection<? extends FlightRoute> getRoutes() {
		return new ArrayList<FlightRoute>(_routes);
	}
	
	/**
	 * Returns whether any routes have been loaded.
	 * @return TRUE if routes have been loaded, otherwise FALSE
	 */
	public boolean hasRoutes() {
		return !_routes.isEmpty();
	}
	
	/**
	 * Loads METARS for the departure and arrival Airports.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void loadWeather() throws DAOException {
		GetWeather wxdao = new GetWeather(_c);
		_mD = wxdao.getMETAR(_rp.getAirportD().getICAO());
		_mA = wxdao.getMETAR(_rp.getAirportA().getICAO());
	}
	
	/**
	 * Loads ACARS Dispatch routes from the database.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void loadDispatchRoutes() throws DAOException {
		GetACARSRoute rdao = new GetACARSRoute(_c);
		Collection<? extends FlightRoute> routes = rdao.getRoutes(_rp, true);
		_routes.addAll(routes);
		log.info("Loaded {} Dispatch Routes", Integer.valueOf(routes.size()));
	}
	
	/**
	 * Loads cached FlightAware routes from the database.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void loadCachedRoutes() throws DAOException {
		GetCachedRoutes rcdao = new GetCachedRoutes(_c);
		Collection<? extends FlightRoute> routes = rcdao.getRoutes(_rp, false);
		log.info("Loaded {} Cached FlightAware Routes", Integer.valueOf(routes.size()));
		_routes.addAll(routes);
	}
	
	/**
	 * Loads flight routes from FlightAware via FlightXML.
	 * @param saveRoutes TRUE if the fetched routes should be saved in the database, otherwise FALSE
	 * @throws DAOException if an I/O or JDBC error occurs
	 */
	public void loadFlightAwareRoutes(boolean saveRoutes) throws DAOException {
		boolean isUS = (_rp.getAirportD().getCountry().getCode().equals("US")) || (_rp.getAirportA().getCountry().getCode().equals("US"));
		if (!isUS) {
			log.info("{} to {} not a US route", _rp.getAirportD(), _rp.getAirportA());
			return;
		}
		
		GetFARoutes fwdao = new GetFARoutes();
		fwdao.setKey(SystemData.get("schedule.flightaware.flightXML.v4"));
		Collection<? extends FlightRoute> routes = fwdao.getRouteData(_rp);
		log.info("Loaded " + routes.size() + " FlightAware Routes");
		_routes.addAll(routes);
		
		// Save in the cache
		if (saveRoutes) {
			try {
				_c.setAutoCommit(false);
				SetCachedRoutes rcwdao = new SetCachedRoutes(_c);
				rcwdao.purge(_rp);
				if (!routes.isEmpty())
					rcwdao.write(routes);
				
				_c.commit();
				log.info("Saved {} FlightAware Routes", Integer.valueOf(routes.size()));
			} catch (Exception e) {
				throw new DAOException(e);
			}
		}
	}
	
	/**
	 * Loads flight routes from filed ACARS Flight Reports.
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void loadPIREPRoutes(String dbName) throws DAOException {
		GetFlightReportRoutes frrdao = new GetFlightReportRoutes(_c);
		Collection<? extends FlightRoute> routes = frrdao.getRoutes(_rp, dbName);
		log.info("Loaded {} Flight Report Routes", Integer.valueOf(routes.size()));
		_routes.addAll(routes);
	}
	
	/*
	 * Gets runway popularity for an aiport based on usage and weather.
	 */
	private List<? extends Runway> getPopularRunways(boolean isTakeoff) throws DAOException {
		METAR m = isTakeoff ? _mD : _mA;
		UsageWindFilter rf = new UsageWindFilter(10, -7);
		rf.setWinds(m);
		
		GetNavData nddao = new GetNavData(_c);
		GetRunwayUsage ardao = new GetRunwayUsage(_c);
		List<Runway> runways = nddao.getRunways(isTakeoff ? _rp.getAirportD() : _rp.getAirportA(), Simulator.P3Dv4);
		RunwayUsage ru = ardao.getPopularRunways(_rp, isTakeoff);
		
		List<? extends Runway> rwys = rf.filter(ru.apply(runways));
		if ((m != null) && (m.getWindSpeed() > 0))
			rwys.sort(new RunwayComparator(m.getWindDirection(), m.getWindSpeed(), true));
		
		return rwys;
	}
	
	/**
	 * Calculates the "best" SID and STAR for each route, taking into consideration winds and runway popularity.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void calculateBestTerminalRoute() throws DAOException {
		List<? extends Runway> dRwys = getPopularRunways(true);
		List<? extends Runway> aRwys = getPopularRunways(false);
		String dRwy = dRwys.isEmpty() ? null : "RW" + dRwys.get(0).getName();
		String aRwy = aRwys.isEmpty() ? null : "RW" + aRwys.get(0).getName();
		if (_preferredRunway == null)
			_preferredRunway = dRwy;

		GetNavRoute navdao = new GetNavRoute(_c);
		for (Iterator<FlightRoute> i = _routes.iterator(); i.hasNext(); ) {
			FlightRoute rt = i.next();
			
			// Get best SID
			if (!StringUtils.isEmpty(rt.getSID()) && (rt.getSID().contains("."))) {
				log.info("Searching for best SID for {} runway {}", rt.getSID(), dRwy);
				List<String> tkns = StringUtils.split(rt.getSID(), ".");
				String sidName = TerminalRoute.makeGeneric(tkns.get(0));
				TerminalRoute sid = navdao.getBestRoute(_rp.getAirportD(), TerminalRoute.Type.SID, sidName, tkns.get(1), _preferredRunway);
				if (sid == null)
					sid = navdao.getBestRoute(_rp.getAirportD(), TerminalRoute.Type.SID, sidName, tkns.get(1), dRwy);
				if (sid != null) {
					log.info("Found {}", sid.getCode());
					rt.setSID(sid.getCode());
				}
			}
			
			// Get best STAR
			if (!StringUtils.isEmpty(rt.getSTAR()) && (rt.getSTAR().contains("."))) {
				log.info("Searching for best STAR for {}", rt.getSTAR());
				List<String> tkns = StringUtils.split(rt.getSTAR(), ".");
				String arrRwy = (tkns.size() < 3) ? aRwy : tkns.get(2);

				// Load the STAR - if we can't find based on the runway, try the most popular
				String starName = TerminalRoute.makeGeneric(tkns.get(0));
				TerminalRoute star = navdao.getBestRoute(_rp.getAirportA(), TerminalRoute.Type.STAR, starName, tkns.get(1), arrRwy);
				if (star == null)
					star = navdao.getBestRoute(_rp.getAirportA(), TerminalRoute.Type.STAR, starName, tkns.get(1), aRwy);
				if (star != null) {
					log.info("Found {}", star.getCode()); 
					rt.setSTAR(star.getCode());
				}
			}
		}
	}
	
	/**
	 * Populates the routes with waypoint data. Even if the route was originally populated, this step is required since
	 * the SIDs and STARs may have been updated based on weather data.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void populateRoutes() throws DAOException {
		GetNavRoute navdao = new GetNavRoute(_c);
		Collection<String> rts = new HashSet<String>();
		Collection<PopulatedRoute> popRoutes = new ArrayList<PopulatedRoute>();
		for (Iterator<FlightRoute> i = _routes.iterator(); i.hasNext(); ) {
			FlightRoute rp = i.next();
			if (!rts.add(rp.getRoute())) {
				log.info("Removed duplicate route {}", rp.getRoute());
				continue;
			}
			
			popRoutes.add(navdao.populate(rp));
		}
		
		_routes.clear();
		_routes.addAll(popRoutes);
	}
}