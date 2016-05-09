// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.Helper;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.RoutePair;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A utility class to build a flight route.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

@Helper(FlightReport.class)
public class RouteBuilder {

	private final String _sid;
	private final String _star;
	
	private final LinkedList<String> _wps = new LinkedList<String>();
	private final Collection<NavigationDataBean> _pts = new LinkedHashSet<NavigationDataBean>();

	/**
	 * Creates the builder. This will parse the provided route to identify a filed SID or STAR
	 * @param f the RoutePair
	 * @param route the filed route
	 */
	public RouteBuilder(RoutePair f, String route) {
		super();
		List<String> wps = StringUtils.nullTrim(StringUtils.split(route, " "));
		if (!CollectionUtils.isEmpty(wps)) {
			_wps.addAll(wps);
			_wps.remove(f.getAirportD().getICAO());
			_wps.remove(f.getAirportA().getICAO());
		}
		
		if ((_wps.size() > 1) && TerminalRoute.isNameValid(_wps.getFirst())) {
			_sid = _wps.getFirst();
			_wps.removeFirst();
		} else
			_sid = null;
		
		if ((_wps.size() > 1) && TerminalRoute.isNameValid(_wps.getLast())) {
			_star = _wps.getLast();
			_wps.removeLast();
		} else
			_star = null;
	}
	
	/**
	 * Returns the SID name.
	 * @return the SID name, or null if none detected
	 */
	public String getSID() {
		return _sid;
	}
	
	/**
	 * Returns the SID transition.
	 * @return the SID transition waypoint, or null if none detected
	 */
	public String getSIDTransition() {
		return (_sid == null) ? null : _wps.getFirst();
	}
	
	/**
	 * Returns the STAR name.
	 * @return the STAR name, or null if none detected
	 */
	public String getSTAR() {
		return _star;
	}
	
	/**
	 * Returns the STAR transition.
	 * @return the STAR transition waypoint, or null if none detected
	 */
	public String getSTARTransition() {
		return (_star == null) ? null : _wps.getLast();
	}
	
	/**
	 * Returns the filed route, minus any SID/STAR names.
	 * @return the route
	 */
	public String getRoute() {
		return StringUtils.listConcat(_wps, " ");
	}

	/**
	 * Returns the waypoints in the route, including the SID/STAR.
	 * @return a List of NavigationDataBeans 
	 */
	public List<NavigationDataBean> getPoints() {
		return new ArrayList<NavigationDataBean>(_pts);
	}
	
	public boolean hasData() {
		return (_pts.size() > 0);
	}
	
	/**
	 * Adds a waypoint to the route.
	 * @param ndb a NavigationDataBean
	 */
	public void add(NavigationDataBean ndb) {
		_pts.add(ndb);
	}

	/**
	 * Adds a TerminalRoute to the waypoints.
	 * @param tr a TerminalRotue bean
	 */
	public void add(TerminalRoute tr) {
		if (tr == null) return;
		String transition = (_wps.isEmpty()) ? null : ((tr.getType() == TerminalRoute.Type.SID) ? _wps.getFirst() : _wps.getLast());
		_pts.addAll(tr.getWaypoints(transition));
	}

	@Override
	public String toString() {
		return getRoute();
	}
}