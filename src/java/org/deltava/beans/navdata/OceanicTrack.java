// Copyright 2004, 2005, 2006, 2007, 2009, 2010, 2013, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store Oceanic Track (NAT/PACOT/AUSOT) information.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class OceanicTrack extends Airway implements OceanicTrackInfo {

    private Instant _date;
    private final Type _routeType;
    private final String _trackID;
    
    /**
     * Creates a new Oceanic Route.
     * @param type the route Type
     * @param code the route code
     */
    public OceanicTrack(Type type, String code) {
        super(type.name() + code, 1);
        _routeType = type;
        _trackID = code.toUpperCase();
        setHighLevel(true);
    }
    
    /**
     * Returns the date of the Oceanic Route NOTAM.
     * @return the route date
     * @see OceanicTrack#setDate(Instant)
     */
    @Override
    public Instant getDate() {
        return _date;
    }
    
    /**
     * Returns the route type code.
     * @return the route type
     */
    @Override
    public Type getType() {
        return _routeType;
    }
    
    /**
     * Returns the direction of this Oceanic Track, based on waypoints.
     * @return the Direction
     */
    public Direction getDirection() {
		if (_waypoints.size() < 2)
			return Direction.EAST;
		
		// Get the first two waypoints
		NavigationDataBean wp1 = _waypoints.get(0);
		NavigationDataBean wp2 = _waypoints.get(1);
		return (wp1.getLongitude() < wp2.getLongitude()) ? Direction.EAST : Direction.WEST;
    }
    
    /**
     * Reverses an Oceanic Track's waypoints.
     * @return an OceanicTrack with reversed waypoints
     */
    public OceanicTrack reverse() {
    	OceanicTrack ot = new OceanicTrack(_routeType, _trackID);
    	ot.setDate(getDate());
    	_waypoints.forEach(ot::addWaypoint);
    	Collections.reverse(ot._waypoints);
    	return ot;
    }
    
	/**
	 * Returns whether the track is fixed from day to day.
	 * @return FALSE always
	 */
	@SuppressWarnings("static-method")
	public boolean isFixed() {
		return false;
	}
    
    /**
     * Returns the Track ID.
     * @return the track ID
     */
    public String getTrack() {
    	return _trackID;
    }
    
    /**
     * Returns the first waypoint on this track.
     * @return the first waypoint bean
     * @see OceanicTrack#getEnd()
     */
    public NavigationDataBean getStart() {
    	return _waypoints.getFirst();
    }
    
    /**
     * Returns the last waypoint on this track.
     * @return the last waypoint bean
     * @see OceanicTrack#getStart()
     */
    public NavigationDataBean getEnd() {
    	return _waypoints.getLast();
    }
    
    /**
     * Updates the NOTAM effective date.
     * @param d the NOTAM date
     * @see OceanicTrackInfo#getDate()
     */
    public void setDate(Instant d) {
        _date = d;
    }
    
	/**
	 * Adds a waypoint to the Oceanic Track. This preserves the airway code for the Navigation data bean
	 * if it appears to have multiple airways listed (for the NAT/PACOT plotters).
	 * @param nd the waypoint
	 * @see Airway#getWaypoints()
	 */
    @Override
	public void addWaypoint(NavigationDataBean nd) {
		String awy = nd.getAirway();
		super.addWaypoint(nd);
		if ((awy != null) && (awy.indexOf(',') != -1))
			nd.setAirway(awy);
	}
    
    /**
     * Checks if a route waypoint is really an Oceanic Track name.
     * @param name the waypoint name
     * @return TRUE if a valid Oceanic Track name, otherwise FALSE
     */
    public static boolean isNameValid(String name) {
    	String n = name.toUpperCase(); boolean lastChar = Character.isLetter(n.charAt(n.length() - 1));
    	for (Type t : Type.values()) {
    		if (n.startsWith(t.name()) && lastChar && (n.length() == (t.name().length() + 1)))
    			return true;
    		if (n.startsWith("NAT") && lastChar && (n.length() == 5) && (n.charAt(3) == 'S'))
    			return true;
    	}

    	return false;
    }
  
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_routeType.name());
		buf.append(_trackID);
		buf.append('-');
		buf.append(_date.toString());
		return buf.toString();
	}
}