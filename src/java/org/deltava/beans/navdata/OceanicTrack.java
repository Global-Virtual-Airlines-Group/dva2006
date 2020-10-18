// Copyright 2004, 2005, 2006, 2007, 2009, 2010, 2013, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;
import java.time.Instant;

import org.deltava.util.cache.ExpiringCacheable;

/**
 * A bean to store Oceanic Track (NAT/PACOT/AUSOT) information.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class OceanicTrack extends Airway implements OceanicTrackInfo {
	
	public static final Collection<? extends OceanicTrack> CONC_ROUTES = List.of(new ConcordeNAT("SM", Direction.WEST, "5015N", "5020N", "5030N", "4940N", "4750N", "4653N", "4460N", "4265N", "4267N"), 
		new ConcordeNAT("SN", Direction.EAST, "4067N", "4165N", "4360N", "4552N", "4550N", "4840N", "4930N", "4920N", "4915N"), new ConcordeNAT("SO", Direction.ALL, "4815N", "4820N", "4830N", "4740N", "4452N", "4260N"), 
		new ConcordeNAT("SP", Direction.ALL, "4720N", "4524N", "4230N", "3440N"));

	private static class ConcordeNAT extends OceanicTrack implements ExpiringCacheable {
		private final Direction _d;

		ConcordeNAT(String track, Direction d, String... route) {
			super(Type.NAT, track);
			_d = d;
			for (String wpt : route) {
				Intersection wp = Intersection.parse(wpt);
				wp.setAirway("NAT" + getTrack());
				addWaypoint(wp);
			}
		}

		@Override
		public final boolean isFixed() {
			return true;
		}
		
		@Override
		public final Direction getDirection() {
			return _d;
		}

		@Override
		public Instant getExpiryDate() {
			return Instant.MAX;
		}
	}

    private Instant _date;
    private final Type _routeType;
    private final String _trackID;
    
    /**
     * Creates a new Oceanic Route for a given data.
     * @param type the route Type
     * @param code the route code
     * @throws IllegalArgumentException if type is invalid
     */
    public OceanicTrack(Type type, String code) {
        super(code, 1);
        _routeType = type;
        _trackID = code.toUpperCase();
        setCode(type.name() + code);
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
    	List<NavigationDataBean> wpl = getWaypoints();
		if (wpl.size() < 2)
			return Direction.EAST;
		
		// Get the first two waypoints
		NavigationDataBean wp1 = wpl.get(0);
		NavigationDataBean wp2 = wpl.get(1);
		return (wp1.getLongitude() < wp2.getLongitude()) ? Direction.EAST : Direction.WEST;
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
  
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_routeType.name());
		buf.append(_trackID);
		buf.append('-');
		buf.append(_date.toString());
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