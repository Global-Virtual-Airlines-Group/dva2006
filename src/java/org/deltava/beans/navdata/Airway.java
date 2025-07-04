// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store Airway names and waypoint data. Since there can be multiple Airways across the world with the
 * same code, each intersection can be flagged as "end of sequence" deliniating the end of a particular airway sequence.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class Airway implements Comparable<Airway>, Route, GeoLocation {

	private String _code;
	protected int _awseq;
	private GeoPosition _pos;
	private boolean _highLevel;
	private boolean _lowLevel;
	
	/**
	 * Waypoints.
	 */
	protected final LinkedList<NavigationDataBean> _waypoints = new LinkedList<NavigationDataBean>();
	
	/**
	 * Creates a new Airway bean.
	 * @param code the airway code
	 * @param seq the airway sequence code
	 * @throws NullPointerException if code is null
	 */
	public Airway(String code, int seq) {
		super();
		setCode(code);
		_awseq = Math.max(0, seq);
	}

	/**
	 * Returns the airway code.
	 * @return the code
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns the sequence number, used to differentiate airways with different locations but the same code.
	 * @return the squence
	 */
	public int getSequence() {
		return _awseq;
	}

	/**
	 * Returns the number of waypoints in the airway.
	 * @return the number of waypoints
	 */
	@Override
	public int getSize() {
		return _waypoints.size();
	}
	
	/**
	 * Returns if this Airway is a low-level Airway. 
	 * @return TRUE if a low-level airway, otherwise FALSE
	 */
	public boolean isLowLevel() {
		return _lowLevel;
	}
	
	/**
	 * Returns if this Airway is a high-level Airway. 
	 * @return TRUE if a high-level airway, otherwise FALSE
	 */
	public boolean isHighLevel() {
		return _highLevel;
	}

	/**
	 * Returns the waypoints for this Airway. <i>The returned collection is mutable.</i>
	 * @return an ordered Collection of waypoints
	 * @see Airway#getRoute()
	 * @see Airway#addWaypoint(NavigationDataBean)
	 */
	@Override
	public LinkedList<NavigationDataBean> getWaypoints() {
		return new LinkedList<NavigationDataBean>(_waypoints);
	}
	
	/**
	 * Returns the latitude of the middle of the Airway.
	 */
	@Override
	public double getLatitude() {
		if (_pos == null)
			calcLocation();
		
		return _pos.getLatitude();
	}

	/**
	 * Returns the longitude of the middle of the Airway.
	 */
	@Override
	public double getLongitude() {
		if (_pos == null)
			calcLocation();
		
		return _pos.getLongitude();
	}
	
	private void calcLocation() {
		double lat = 0; double lng = 0;
		for (GeoLocation loc : _waypoints) {
			lat += loc.getLatitude();
			lng += loc.getLongitude();
		}
		
		_pos = new GeoPosition(lat / _waypoints.size(), lng / _waypoints.size());
	}
	
	/**
	 * Finds a waypoint in this Airway.
	 * @param code the waypoint code
	 * @return the offset within the waypoints, or -1 if not found
	 */
	protected int find(String code) {
		for (int pos = 0; pos < _waypoints.size(); pos++) {
			if (_waypoints.get(pos).getCode().equalsIgnoreCase(code)) 
				return pos;
		}
		
		return -1;
	}
	
	/**
	 * Returns whether this Airway contains a particular waypoint.
	 * @param code the waypoint code
	 * @return TRUE if found, otherwise FALSE
	 */
	public boolean contains(String code) {
		return (find(code) != -1);
	}

	/**
	 * Returns a subset of waypoints for this Airway between two waypoints.
	 * @param start the starting waypoint code
	 * @param end the ending waypoint code
	 * @return a List of waypoint codes
	 * @throws NullPointerException if start or end are null
	 */
	public List<NavigationDataBean> getWaypoints(String start, String end) {
		int st = find(start);
		int ed = find(end);
		if ((start == null) || ((st == -1) && (end == null)))
			st = 0;
		if ((st == -1) && (end != null))
			return new ArrayList<NavigationDataBean>();
		else if (ed == -1)
			ed = _waypoints.size() - 1;
		else if (ed < st) {
			// If ed is before sd then reverse the waypoints
			List<NavigationDataBean> wp2 = new ArrayList<NavigationDataBean>(_waypoints.subList(ed, st+1));
			Collections.reverse(wp2);
			return wp2;
		}
		
		return new ArrayList<NavigationDataBean>(_waypoints.subList(st, ed+1));
	}
	
	/**
	 * Returns the Airway route.
	 * @return a space-delimited list of waypoint codes
	 */
	@Override
	public String getRoute() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _waypoints.iterator(); i.hasNext();) {
			buf.append(i.next().getCode());
			if (i.hasNext())
				buf.append(' ');
		}

		return buf.toString();
	}

	/**
	 * Adds a waypoint to the Airway route.
	 * @param nd the waypoint
	 * @see Airway#getWaypoints()
	 */
	@Override
	public void addWaypoint(NavigationDataBean nd) {
		_waypoints.add(nd);
		nd.setAirway(_code);
		_pos = null;
	}
	
	/**
	 * Updates the Airway code.
	 * @param code the code
	 * @throws NullPointerException if code is null
	 */
	public void setCode(String code) {
		_code = code.trim().toUpperCase();
	}
	
	/**
	 * Marks this Airway as a low-level Airway.
	 * @param isLow TRUE if low-level, otherwise FALSE
	 */
	public void setLowLevel(boolean isLow) {
		_lowLevel = isLow;
	}
	
	/**
	 * Marks this Airway as a high-level Airway.
	 * @param isHigh TRUE if high-level, otherwise FALSE
	 */
	public void setHighLevel(boolean isHigh) {
		_highLevel = isHigh;
	}

	@Override
	public int compareTo(Airway a2) {
		int tmpResult = _code.compareTo(a2._code);
		return (tmpResult == 0) ? Integer.compare(_awseq, a2._awseq) : tmpResult;
	}
	
	@Override
	public boolean equals(Object o2) {
		return (o2 instanceof Airway a2) ? (compareTo(a2) == 0) : false;
	}
	
	@Override
	public int hashCode() {
		return (_code + "-" + String.valueOf(_awseq)).hashCode();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public Object cacheKey() {
		return getCode();
	}
}