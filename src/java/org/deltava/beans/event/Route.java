package org.deltava.beans.event;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.schedule.Airport;

/**
 * A class to store information about a Flight Route for an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Route extends DatabaseBean {
	
	private Airport _airportD;
	private Airport _airportA;
	private String _route;
	
	/**
	 * Creates a new Event Route.
	 * @param eventID the Event database ID
	 * @param route the flight route
	 */
	public Route(int eventID, String route) {
		super();
		setID(eventID);
		setRoute(route);
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
     * Updates the flight route for this Route.
     * @param route the waypoints
     * @see Route#getRoute()
     */
    public void setRoute(String route) {
    	if (route != null)
    		_route = route.replaceAll("[.]+", " ");
    }
}