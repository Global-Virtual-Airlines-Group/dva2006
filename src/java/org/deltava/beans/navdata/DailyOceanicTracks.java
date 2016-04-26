// Copyright 2010, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store a daily collection of Oceanic Tracks. 
 * @author Luke
 * @version 5.1
 * @since 3.4
 */

public class DailyOceanicTracks implements java.io.Serializable, Comparable<DailyOceanicTracks>, OceanicTrackInfo {
	
	private Instant _effectiveDate;
	private final Type _type;
	private final Map<String, OceanicTrack> _tracks = new TreeMap<String, OceanicTrack>();

	/**
	 * Creates the bean.
	 * @param t the Track type
	 * @param effDate the effective date
	 */
	public DailyOceanicTracks(Type t, Instant effDate) {
		super();
		_type = t;
		_effectiveDate = effDate;
	}

	@Override
	public Instant getDate() {
		return _effectiveDate;
	}
	
	@Override
	public Type getType() {
		return _type;
	}
	
	/**
	 * Returns the number of OceanicTracks in the collection.
	 * @return the number of tracks
	 */
	public int size() {
		return _tracks.size();
	}
	
	/**
	 * Adds a Track to this collection.
	 * @param t an OceanicTrack bean
	 * @throws NullPointerException if t is null
	 * @throws IllegalArgumentException if the track type or date do not match
	 */
	public void addTrack(OceanicTrack t) {
		if (t.getType() != _type)
			throw new IllegalArgumentException("Cannot add " + t.getType() + " to " + _type + " collection");
		if (_effectiveDate == null)
			_effectiveDate = t.getDate();
		
		_tracks.put(t.getCode(), t);
	}
	
	/**
	 * Returns an Oceanic Track with a specific code
	 * @param code the Track code, which includes the track type and ID
	 * @return an OceanicTrack bean, or null if not found
	 * @throws NullPointerException if code is null
	 */
	public OceanicTrack getTrack(String code) {
		return _tracks.get(code.toUpperCase());
	}
	
	/**
	 * Find an oceanic track with the specific start and end waypoints.
	 * @param startWP the starting waypoint code, or null if any
	 * @param endWP the ending waypoint code, or null if any
	 * @return an OceanicTrack bean, or null if none found
	 */
	public OceanicTrack find(String startWP, String endWP) {
		for (OceanicTrack t : _tracks.values()) {
			if (t.getSize() == 0)
				continue;
			
			boolean isMatch = (startWP == null) ? true : startWP.equalsIgnoreCase(t.getStart().getCode());
			isMatch &= (endWP == null) ? true : endWP.equalsIgnoreCase(t.getEnd().getCode());
			if (isMatch)
				return t;
		}
		
		return null;
	}
	
	/**
	 * Returns if any Oceanic Track contains a particular waypoint.
	 * @param code the waypoint code
	 * @return TRUE if any track contains this waypoint, otherwise FALSE
	 */
	public boolean contains(String code) {
		for (OceanicTrack t : _tracks.values()) {
			if (t.contains(code))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns all of the Track for that day.
	 * @return a Collection of OceanicTrack beans
	 */
	public Collection<OceanicTrack> getTracks() {
		return new ArrayList<OceanicTrack>(_tracks.values());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof DailyOceanicTracks) ? (compareTo((DailyOceanicTracks) o) == 0) : false;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_type.name());
		buf.append("S-");
		buf.append(_effectiveDate);
		return buf.toString();
	}

	/**
	 * Compares two Track collections by comparing their type and effective dates.
	 */
	@Override
	public int compareTo(DailyOceanicTracks ot2) {
		int tmpResult = _type.compareTo(ot2._type);
		return (tmpResult == 0) ? _effectiveDate.compareTo(ot2._effectiveDate) : tmpResult;
	}
}