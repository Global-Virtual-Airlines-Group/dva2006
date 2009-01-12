// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store saved ACARS dispatch routes.
 * @author Luke
 * @version 2.4
 * @since 2.0
 */

public class DispatchRoute extends FlightRoute implements AuthoredBean, ViewEntry {
	
	private int _authorID;
	private Date _lastUsed;
	
	private Airline _a;
	private Airport _airportL;
	
	private boolean _active;

	private int _useCount;
	private int _dspBuild;
	
	private final Map<NavigationDataBean, String> _route = new LinkedHashMap<NavigationDataBean, String>(); 

	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the number of times this route has been used.
	 * @return the usage count
	 */
	public int getUseCount() {
		return _useCount;
	}
	
	/**
	 * Returns the last use date of this route.
	 * @return the last use date/time
	 */
	public Date getLastUsed() {
		return _lastUsed;
	}
	
	/**
	 * Returns the build number of the dispatch client used to create this route.
	 * @return the build number, or zero if via web application
	 */
	public int getDispatchBuild() {
		return _dspBuild;
	}
	
	/**
	 * Returns the Airline for this Route.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the alternate Airport.
	 * @return the Airport, or null if none
	 */
	public Airport getAirportL() {
		return _airportL;
	}
	
	/**
	 * Returns the route.
	 * @return a space-separated list of waypoints and airways
	 */
	public String getRoute() {
		String rt = super.getRoute();
		if (rt != null)
			return rt;
		
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _route.keySet().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			buf.append(nd.getCode());
			if (i.hasNext())
				buf.append(' ');
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the waypoints on this route.
	 * @return a Collection of NavigationDataBeans
	 */
	public Collection<NavigationDataBean> getWaypoints() {
		return _route.keySet();
	}
	
	/**
	 * Returns the Airway each waypoint is on.
	 * @param nd the waypoint
	 * @return the Airway code, or null if none
	 */
	public String getAirway(NavigationDataBean nd) {
		return _route.get(nd);
	}
	
	/**
	 * Adds a waypoint to the route.
	 * @param nd the waypoint
	 * @param airway the airway it is on, or null
	 */
	public void addWaypoint(NavigationDataBean nd, String airway) {
		_route.put(nd, (airway == null) ? "" : airway);
	}

	/**
	 * Updates the last use date of this route.
	 * @param dt the last use date/time
	 */
	public void setLastUsed(Date dt) {
		_lastUsed = dt;
	}
	
	/**
	 * Sets the build number of the dispatch client used to create this route.
	 * @param build the build number, or zero if via web application
	 */
	public void setDispatchBuild(int build) {
		_dspBuild = Math.max(0, build);
	}
	
	/**
	 * Updates the number of times this route has been used.
	 * @param count the usage count
	 */
	public void setUseCount(int count) {
		_useCount = Math.max(0, count);
	}
	
	/**
	 * Returns if this route is active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Updates the Airline.
	 * @param a the Airline
	 */
	public void setAirline(Airline a) {
		_a = a;
	}
	
	/**
	 * Updates the alternate Airport.
	 * @param a the Airport
	 */
	public void setAirportL(Airport a) {
		_airportL = a;
	}
	
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Marks this route as active.
	 * @param isActive TRUE if active, otherwise false 
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	public String getComboName() {
		return getRoute();
	}
	
	public String getComboAlias() {
		return getHexID();
	}
	
	public String getRowClassName() {
		return _active ? null : "warn";
	}
}