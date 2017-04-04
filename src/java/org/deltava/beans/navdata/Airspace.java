// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.Country;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to define arbitrary airspaces. 
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class Airspace implements Comparable<Airspace>, Cacheable {
	
	private final String _id;
	private String _name;
	private final AirspaceType _type;
	private boolean _isExclude;
	
	private Country _c;
	
	private int _minAlt;
	private int _maxAlt;
	
	private final Collection<GeoLocation> _border = new LinkedHashSet<GeoLocation>();

	/**
	 * Creates the bean.
	 * @param id the airspace code
	 * @param type the AirspaceType
	 */
	public Airspace(String id, AirspaceType type) {
		super();
		_id = id.trim().toUpperCase();
		_type = type;
	}

	/**
	 * Returns the Airspace name.
	 * @return the name
	 * @see FIR#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Airspace ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the Airspace type.
	 * @return an AirspaceType
	 */
	public AirspaceType getType() {
		return _type;
	}
	
	/**
	 * Returns the coordinates of the FIR boundary.
	 * @return a Collection of GeoLocations
	 * @see FIR#addBorderPoint(GeoLocation)
	 */
	public Collection<GeoLocation> getBorder() {
		return new ArrayList<GeoLocation>(_border);
	}
	
	/**
	 * Returns the nation containing this Airspace.
	 * @return a Country, or null if unknown
	 */
	public Country getCountry() {
		return _c;
	}
	
	/**
	 * Returns the maximum altitude of this airspace.
	 * @return the altitude in feet MSL
	 */
	public int getMaxAltitude() {
		return _maxAlt;
	}
	
	/**
	 * Returns the minimum altitude of this airspace.
	 * @return the altitude in feet MSL
	 */
	public int getMinAltitude() {
		return _minAlt;
	}
	
	/**
	 * Returns whether this is an exclusion zone of an existing Airspace.
	 * @return TRUE if an exclusion zone, otherwise FALSE
	 */
	public boolean isExclusion() {
		return _isExclude;
	}
	
	/**
	 * Adds a point to the airspace boundary.
	 * @param loc a GeoLocation
	 * @see Airspace#getBorder()
	 */
	public void addBorderPoint(GeoLocation loc) {
		_border.add(loc);
	}
	
	/**
	 * Sets the Airspace name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Airspace#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Sets whether this is an exclusion zone of an existing Airspace.
	 * @param isExclusion TRUE if an exclusion zone, otherwise FALSE
	 */
	public void setExclusion(boolean isExclusion) {
		_isExclude = isExclusion;
	}
	
	/**
	 * Sets the nation containing this Airspace.
	 * @param c a Country, or null
	 */
	public void setCountry(Country c) {
		_c = c;
	}
	
	/**
	 * Sets the maximum altitude of the Airspace.
	 * @param alt the altitude in feet MSL
	 */
	public void setMaxAltitude(int alt) {
		_maxAlt = Math.max(_minAlt, alt);
	}
	
	/**
	 * Sets the minimum altitude of the Airspace.
	 * @param alt the altitude in feet MSL
	 */
	public void setMinAltitude(int alt) {
		_minAlt = Math.min(_maxAlt, alt);
	}

	@Override
	public Object cacheKey() {
		return (_id + "!!" + _c.getCode());
	}
	
	/**
	 * Compares two Airspaces by comparing their types and IDs.
	 */
	@Override
	public int compareTo(Airspace a2) {
		int tmpResult = _type.compareTo(a2._type);
		return (tmpResult == 0) ? _id.compareTo(a2._id) : tmpResult;
	}
}