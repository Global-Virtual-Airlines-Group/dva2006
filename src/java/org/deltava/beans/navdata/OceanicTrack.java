// Copyright 2004, 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store Oceanic Track (NAT/PACOT) information.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class OceanicTrack extends Airway implements OceanicTrackInfo {
	
	public static final Collection<? extends OceanicTrack> CONC_ROUTES = Arrays.asList(new ConcordeNAT("SM",
		"5015N,5020N,5030N,4840N,4750N"), new ConcordeNAT("SN", "4550N,4740N,4930N,4920N,4915N"), 
		new ConcordeNAT("SO", "4815N,4820N,4830N,4640N,4450N,4260N"));

	private static class ConcordeNAT extends OceanicTrack {

		ConcordeNAT(String track, String route) {
			super(Type.NAT, track);
			setDate(new Date());
			for (Iterator<String> i = StringUtils.split(route, ",").iterator(); i.hasNext();) {
				String wpCode = i.next();
				Intersection wp = Intersection.parse(wpCode);
				wp.setAirway("NAT" + getTrack());
				addWaypoint(wp);
			}
		}

		public boolean isFixed() {
			return true;
		}
	}

    private Date _date;
    private Type _routeType;
    
    private String _trackID;
    
    /**
     * Creates a new Oceanic Route for a given data.
     * @param type the route Type
     * @throws IllegalArgumentException if type is invalid
     */
    public OceanicTrack(Type type, String code) {
        super(code, 1);
        setType(type);
        setTrack(code);
    }
    
    /**
     * Returns the date of the Oceanic Route NOTAM.
     * @return the route date
     * @see OceanicTrack#setDate(Date)
     */
    public Date getDate() {
        return _date;
    }
    
    /**
     * Returns the route type code.
     * @return the route type
     * @see OceanicTrackInfo#getTypeName()
     */
    public Type getType() {
        return _routeType;
    }
    
    /**
     * Returns the direction of this Oceanic Track, based on waypoints.
     * @return the Direction
     */
    public Direction getDirection() {
    	List<NavigationDataBean> wpl = getWaypoints();
		if (wpl.size() < 2)
			return Direction.EAST;
		
		// Get the frist two waypoints
		NavigationDataBean wp1 = wpl.get(0);
		NavigationDataBean wp2 = wpl.get(1);
		return (wp1.getLongitude() < wp2.getLongitude()) ? Direction.EAST : Direction.WEST;
    }
    
    /**
     * Returns the route type name.
     * @return the route type name
     * @see OceanicTrackInfo#TYPES
     * @see OceanicTrackInfo#getType()
     */
    public String getTypeName() {
        return OceanicTrackInfo.TYPES[_routeType.ordinal()];
    }
    
	/**
	 * Returns whether the track is fixed from day to day.
	 * @return FALSE
	 */
	public boolean isFixed() {
		return false;
	}
    
    /**
     * Returns the Track ID.
     * @return the track ID
     * @see OceanicTrack#setTrack(String)
     */
    public String getTrack() {
    	return _trackID;
    }
    
    /**
     * Updates the NOTAM effective date.
     * @param d the NOTAM date
     * @see OceanicTrackInfo#getDate()
     */
    public void setDate(Date d) {
        _date = d;
    }
    
    /**
     * Updates the route type.
     * @param type the route type code
     * @see OceanicTrackInfo#getType()
     * @see OceanicTrackInfo#getTypeName()
     */
    public void setType(Type type) {
        _routeType = type;
    }
    
    /**
     * Sets the Track ID, and updates the code to preface the route type in front of the track ID.
     * @param id the Track ID
     * @throws NullPointerException if id is null
     * @see OceanicTrack#getTrack()
     */
    public void setTrack(String id) {
    	_trackID = id.toUpperCase();
    	setCode(getTypeName() + id);
    }
    
	/**
	 * Adds a waypoint to the Oceanic Track. This preserves the airway code for the Navigation data bean
	 * if it appears to have multiple airways listed (for the NAT/PACOT plotters).
	 * @param nd the waypoint
	 * @see Airway#getWaypoints()
	 */
	public void addWaypoint(NavigationDataBean nd) {
		String awy = nd.getAirway();
		super.addWaypoint(nd);
		if ((awy != null) && (awy.indexOf(',') != -1))
			nd.setAirway(awy);
	}
    
	public String toString() {
		StringBuilder buf = new StringBuilder(getTypeName());
		buf.append(getTrack());
		buf.append('-');
		buf.append(getDate().toString());
		buf.append('-');
		for (Iterator<NavigationDataBean> i = getWaypoints().iterator(); i.hasNext(); ) {
			NavigationDataBean wp = i.next();
			buf.append(wp.getCode());
			if (i.hasNext())
				buf.append('.');
		}
		
		return buf.toString();
	}
}