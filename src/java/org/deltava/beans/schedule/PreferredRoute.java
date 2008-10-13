// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store FAA preferred routing data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @deprecated
 */

@Deprecated
public class PreferredRoute extends DatabaseBean {
    
    private Airport _airportD;
    private Airport _airportA;

    private String _artcc;
    private String _route;
    
    /**
     * Create a new Preferred Routing between the two airports.
     * @param ad the departure Airport
     * @param aa the arrival Airport
     * @see PreferredRoute#getAirportA()
     * @see PreferredRoute#getAirportD()
     */
    public PreferredRoute(Airport ad, Airport aa) {
        super();
        _airportD = ad;
        _airportA = aa;
    }
    
    /**
     * Returns the arrival Airport.
     * @return the airport
     */
    public Airport getAirportA() {
        return _airportA;
    }
    
    /**
     * Returns the departure Airport.
     * @return the airport
     */
    public Airport getAirportD() {
        return _airportD;
    }
    
    /**
     * Returns the routing between the airports.
     * @return the routing as a space-delimited string
     * @see PreferredRoute#setRoute(String)
     */
    public String getRoute() {
        return _route;
    }
    
    /**
     * Returns the ARTCCs this route transits.
     * @return the ARTCCs as a space-delimited string
     * @see PreferredRoute#setARTCC(String)
     */
    public String getARTCC() {
        return _artcc;
    }
    
    /**
     * Sets the route between the airports.
     * @param r the route. This will be converted to uppercase.
     * @see PreferredRoute#getRoute()
     * @throws NullPointerException if r is null
     */
    public void setRoute(String r) {
        _route = r.trim().toUpperCase();
    }
    
    /**
     * Sets the ARTCCs this route transits.
     * @param ctrList the list of ARTCCs as a space-delimited string
     * @see PreferredRoute#getARTCC()
     * @throws NullPointerException if ctrList is null
     */
    public void setARTCC(String ctrList) {
        _artcc = ctrList.trim().toUpperCase();
    }
    
    /**
     * Implements Comparable interface by comparing departure and arrival airport codes.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        PreferredRoute pr2 = (PreferredRoute) o2;
        int tmpResult = _airportD.compareTo(pr2.getAirportD());
        return (tmpResult == 0) ? _airportA.compareTo(pr2.getAirportA()) : tmpResult;
    }
}