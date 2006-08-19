// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

/**
 * A bean to store Oceanic Track (NAT/PACOT) information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class OceanicRoute implements java.io.Serializable, Comparable {

    /**
     * Route type for North Atlantic Track (NAT) data.
     */
    public static final int NAT = 0;
    
    /**
     * Route type for Pacific Operations Tracks.
     */
    public static final int PACOT = 1;
    
    /**
     * Track type names.
     */
    public static final String[] TYPES = {"NAT", "PACOT"};
    
    private Date _date;
    private int _routeType;
    
    private String _sourceHost;
    private String _routeInfo;
    
    /**
     * Creates a new Oceanic Route for a given data.
     * @param type the route Type
     * @see OceanicRoute#getType()
     * @see OceanicRoute#getTypeName()
     * @see OceanicRoute#TYPES
     * @see OceanicRoute#setType(int)
     * @throws IllegalArgumentException if type is invalid
     */
    public OceanicRoute(int type) {
        super();
        setType(type);
    }
    
    /**
     * Returns the date of the Oceanic Route NOTAM.
     * @return the route date
     * @see OceanicRoute#setDate(Date)
     */
    public Date getDate() {
        return _date;
    }
    
    /**
     * Returns the source hostname where this NOTAM was downloaded from.
     * @return the hostname
     * @see OceanicRoute#setSource(String)
     */
    public String getSource() {
        return _sourceHost;
    }
    
    /**
     * Returns the route NOTAM text.
     * @return the NOTAM text
     * @see OceanicRoute#setRoute(String)
     */
    public String getRoute() {
        return _routeInfo;
    }
    
    /**
     * Returns the route type code.
     * @return the route type
     * @see OceanicRoute#getTypeName()
     * @see OceanicRoute#setType(int)
     */
    public int getType() {
        return _routeType;
    }
    
    /**
     * Returns the route type name.
     * @return the route type name
     * @see OceanicRoute#TYPES
     * @see OceanicRoute#getType()
     * @see OceanicRoute#setType(String)
     */
    public String getTypeName() {
        return OceanicRoute.TYPES[_routeType];
    }
    
    /**
     * Updates the NOTAM effective date.
     * @param d the NOTAM date
     * @see OceanicRoute#getDate()
     */
    public void setDate(Date d) {
        _date = d;
    }
    
    /**
     * Updates the route type.
     * @param type the route type code
     * @throws IllegalArgumentException if type is negative or invalid
     * @see OceanicRoute#setType(String)
     * @see OceanicRoute#getType()
     * @see OceanicRoute#getTypeName()
     */
    public void setType(int type) {
        if ((type < 0) || (type >= OceanicRoute.TYPES.length))
            throw new IllegalArgumentException("Invalid Oceanic Route type - " + type);
        
        _routeType = type;
    }
    
    /**
     * Updates the route type.
     * @param typeName the route type name
     * @throws IllegalArgumentException if typeName is invalid
     * @see OceanicRoute#setType(int)
     * @see OceanicRoute#getTypeName()
     * @see OceanicRoute#getType()
     */
    public void setType(String typeName) {
        for (int x = 0; x < OceanicRoute.TYPES.length; x++) {
            if (OceanicRoute.TYPES[x].equalsIgnoreCase(typeName)) {
                setType(x);
                return;
            }
        }
        
        // If we got this far, the type Name didn't match
        throw new IllegalArgumentException("Invalid Oceanic Route type - " + typeName);
    }
    
    /**
     * Updates the NOTAM source hostname.
     * @param srcHost the host this NOTAM was downloaded from
     * @throws NullPointerException if srcHost is null
     * @see OceanicRoute#getSource()
     */
    public void setSource(String srcHost) {
        _sourceHost = srcHost.trim().toLowerCase();
    }
    
    /**
     * Updates the NOTAM contents.
     * @param routeData the NOTAM contents
     * @see OceanicRoute#getRoute()
     */
    public void setRoute(String routeData) {
        _routeInfo = routeData;
    }
    
    /**
     * Implements Comparable interface by comparing the dates, then the route types.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        OceanicRoute or2 = (OceanicRoute) o2;
        int tmpResult = _date.compareTo(or2.getDate());
        return (tmpResult == 0) ? new Integer(_routeType).compareTo(new Integer(or2.getType())) : tmpResult;
    }
}