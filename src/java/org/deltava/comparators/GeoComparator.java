// Copyright 2005, 2006, 2008, 2010, 2011, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A comparator to sort geographic locations by their distance from a fixed point.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class GeoComparator implements Comparator<GeoLocation>, java.io.Serializable {

	private final GeoLocation _point;
	private final boolean _useFeet;

	/**
	 * Creates a new GeoComparator, comparing distance from an arbitrary location.
	 * @param loc the location
	 */
	public GeoComparator(GeoLocation loc) {
		this(loc, false);
	}

	/**
	 * Creates a new GeoComparator, comparing distance from an arbitrary location.
	 * @param loc the location
	 * @param useFeet TRUE if the calculation should be measured in feet, otherwise FALSE for miles
	 */
	public GeoComparator(GeoLocation loc, boolean useFeet) {
		super();
		_useFeet = useFeet;
		_point = (loc == null) ? new GeoPosition(0, 0) : loc;
	}

	/**
	 * Returns the reference location.
	 * @return a GeoLocation
	 */
	public GeoLocation getLocation() {
		return _point;
	}

	@Override
	public int compare(GeoLocation l1, GeoLocation l2) {
		int d1 = _useFeet ? _point.distanceFeet(l1) : _point.distanceTo(l1);
		int d2 = _useFeet ? _point.distanceFeet(l2) : _point.distanceTo(l2);
		return Integer.compare(d1, d2);
	}
}