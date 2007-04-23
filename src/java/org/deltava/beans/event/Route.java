// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

/**
 * A class to store information about a Flight Route for an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Route extends DatabaseBean implements Comparable, ComboAlias, ViewEntry {
	
	private Airport _airportD;
	private Airport _airportA;
	private String _route;
	private boolean _active = true;
	
	/**
	 * Creates a new Event Route.
	 * @param eventID the Event database ID
	 * @param route the flight route
	 */
	public Route(int eventID, String route) {
		super();
		setRoute(route);
		if (eventID != 0)
			setID(eventID);
	}
	
    /**
     * Returns the arrival Airport for this Flight Route.
     * @return the Arrival airport object
     * @see Route#setAirportA(Airport)
     * @see Route#getAirportD()
     */
    public Airport getAirportA() {
        return _airportA;
    }
    
    /**
     * Returns the departure Airport for this Flight Route.
     * @return the Departure airport object
     * @see Route#setAirportD(Airport)
     * @see Route#getAirportA()
     */
    public Airport getAirportD() {
        return _airportD;
    }
    
    /**
     * Returns the flight plan for this route.
     * @return the flight plan
     * @see Route#setRoute(String)
     */
    public String getRoute() {
    	return _route;
    }
    
    /**
     * Returns whether the Route is active.
     * @return TRUE if the Route is available for signup, otherwise FALSE
     */
    public boolean getActive() {
    	return _active;
    }

    /**
     * Updates the Arrival airport.
     * @param a the new arrival Airport object
     * @see Route#getAirportA()
     * @see Route#setAirportD(Airport)
     */
    public void setAirportA(Airport a) {
        _airportA = a;
    }
    
    /**
     * Updates the Departure airport.
     * @param a the new departure Airport object
     * @see Route#getAirportD()
     * @see Route#setAirportA(Airport)
     */
    public void setAirportD(Airport a) {
        _airportD = a;
    }
    
    /**
     * Updates whether the Route is Active.
     * @param isActive TRUE if the Route is active, otherwise FALSE 
     */
    public void setActive(boolean isActive) {
    	_active = isActive;
    }
    
    /**
     * Updates the flight route for this Route.
     * @param route the waypoints
     * @see Route#getRoute()
     */
    public void setRoute(String route) {
    	if (route != null)
    		_route = route.replaceAll("[.]+", " ");
    }
    
    public String getComboName() {
    	return toString();
    }
    
    public String getComboAlias() {
    	return _airportD.getIATA() + "-" + _airportA.getIATA();
    }
    
    /**
     * Compare two routes by comparing the names of the departure/arrival airports.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
    	Route r2 = (Route) o;
    	int tmpResult = _airportD.getName().compareTo(r2._airportD.getName());
    	if (tmpResult == 0)
    		tmpResult = _airportA.getName().compareTo(r2._airportA.getName());
    	
    	return tmpResult;
    }
    
    /**
     * Compares a route by comparing the departure and arrival airports.
     */
    public boolean equals(Object o) {
    	return (o instanceof Route) ? (compareTo(o) == 0) : false;
    }
    
    /**
     * Renders this object to a String by appending the airports and codes.
     */
    public String toString() {
    	StringBuilder buf = new StringBuilder(_airportD.getName());
    	buf.append(" (");
    	buf.append(_airportD.getIATA());
    	buf.append(") - ");
    	buf.append(_airportA.getName());
    	buf.append(" (");
    	buf.append(_airportA.getIATA());
    	buf.append(')');
    	return buf.toString();
    }
    
    public String getRowClassName() {
    	return _active ? null : "opt2";
    }
}