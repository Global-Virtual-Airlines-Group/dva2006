// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.navdata.*;
import org.deltava.util.StringUtils;

/**
 * A bean to store waypoint information for an oceanic route.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class OceanicWaypoints extends OceanicRoute implements Comparable<OceanicWaypoints> {
	
	public static final Collection<? extends OceanicWaypoints> CONC_ROUTES = Arrays.asList(new ConcordeNAT("M",
		"5015N,5020N,5030N,4840N,4750N"), new ConcordeNAT("N", "45/50,47/40,49/30,49/20,49/15"), 
		new ConcordeNAT("O", "48/15,48/20,48/30,46/40,44/50,42/60"));

	private static class ConcordeNAT extends OceanicWaypoints {

		ConcordeNAT(String track, String route) {
			super(NAT, new Date());
			setTrack(track);
			for (Iterator<String> i = StringUtils.split(route, ",").iterator(); i.hasNext();) {
				String wp = i.next();
				addWaypoint(Intersection.parseNAT(wp));
			}
		}

		public String getTrack() {
			return "S" + super.getTrack();
		}
		
		public boolean isFixed() {
			return true;
		}
	}
	
	/**
	 * Direction code for an Easterly track.
	 */
	public static final int EAST = 0;
	
	/**
	 * Direction code for an Westerly track.
	 */
	public static final int WEST = 1;
	
	private String _track;
	private final Collection<NavigationDataBean> _waypoints = new LinkedHashSet<NavigationDataBean>();

	/**
	 * Initializes the route. 
	 * @param type the route type
	 * @param dt the effective date
	 */
	public OceanicWaypoints(int type, Date dt) {
		super(type);
		setDate(dt);
	}

	/**
	 * Returns the waypoints on this route.
	 * @return a Collection of {@link Intersection} beans
	 * @see OceanicWaypoints#addWaypoint(NavigationDataBean)
	 */
	public Collection<NavigationDataBean> getWaypoints() {
		return new LinkedHashSet<NavigationDataBean>(_waypoints);
	}
	
	/**
	 * Returns the Oceanic track code.
	 * @return the code
	 * @see OceanicWaypoints#setTrack(String)
	 */
	public String getTrack() {
		return _track;
	}
	
	/**
	 * Returns whether the track is fixed from day to day.
	 * @return FALSE
	 */
	public boolean isFixed() {
		return false;
	}
	
	/**
	 * Returns a space-delimited list of the waypoints in this track.
	 * @return the waypoint codes
	 */
	public String getWaypointCodes() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _waypoints.iterator(); i.hasNext(); ) {
			NavigationDataBean wp = i.next();
			buf.append(wp.getCode());
			if (i.hasNext())
				buf.append(' ');
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the direction for this Oceanic Track.
	 * @return the direction code
	 */
	public int getDirection() {
		if (_waypoints.size() < 2)
			return EAST;
		
		// Get the frist two waypoints
		List<NavigationDataBean> wpl = new ArrayList<NavigationDataBean>(_waypoints);
		NavigationDataBean wp1 = wpl.get(0);
		NavigationDataBean wp2 = wpl.get(1);
		return (wp1.getLongitude() < wp2.getLongitude()) ? EAST : WEST;
	}
	
	/**
	 * Adds a waypoint to the end of this route.
	 * @param i the Intersection
	 * @see OceanicWaypoints#getWaypoints()
	 */
	public void addWaypoint(NavigationDataBean i) {
		_waypoints.add(i);
	}
	
	/**
	 * Updates the track code. Only the first character is used. 
	 * @param code the track code
	 * @throws NullPointerException if code is null
	 * @see OceanicWaypoints#getTrack()
	 */
	public void setTrack(String code) {
		_track = code.substring(0, 1).toUpperCase();
	}
	
	/**
	 * Compares two routes by comparing their dates, track types and route codes.
	 */
	public int compareTo(OceanicWaypoints ow2) {
		int tmpResult = getDate().compareTo(ow2.getDate());
		if (tmpResult == 0)
			tmpResult = new Integer(getType()).compareTo(new Integer(ow2.getType()));
		return (tmpResult == 0) ? tmpResult = _track.compareTo(ow2._track) : tmpResult;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(getTypeName());
		buf.append(getTrack());
		buf.append('-');
		buf.append(getDate().toString());
		buf.append('-');
		for (Iterator<NavigationDataBean> i = _waypoints.iterator(); i.hasNext(); ) {
			NavigationDataBean wp = i.next();
			buf.append(wp.getCode());
			if (i.hasNext())
				buf.append('.');
		}
		
		return buf.toString();
	}
}