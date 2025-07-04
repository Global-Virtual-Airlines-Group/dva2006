// Copyright 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Country;

import org.deltava.util.GeoUtils;
import org.deltava.util.cache.Cacheable;

import com.vividsolutions.jts.geom.*;

/**
 * A bean to define arbitrary airspace boundaries.
 * @author Luke
 * @version 11.0
 * @since 7.3
 */

public class Airspace implements MapEntry, GeospaceLocation, Comparable<Airspace>, Cacheable {
	
	private final String _id;
	private String _name;
	private final AirspaceType _type;
	private boolean _isExclude;
	
	private Country _c;
	
	private int _minAlt;
	private int _maxAlt;
	
	private final Collection<GeoLocation> _border = new LinkedHashSet<GeoLocation>();
	private transient Geometry _geo;
	
	private transient static final Collection<Airspace> _restricted = new LinkedHashSet<Airspace>();
	
	/**
	 * Initializes restricted airspace.
	 * @param data a Collection of Airspace beans
	 */
	public static synchronized void init(Collection<Airspace> data) {
		data.stream().filter(a -> ((a.getType() == AirspaceType.P) || (a.getType() == AirspaceType.R))).forEach(_restricted::add);
	}
	
	/**
	 * Returns whether a particular point is in restricted airspace.
	 * @param loc the GeospaceLocation
	 * @return the Airspace bean, or null if none
	 */
	public static Airspace isRestricted(GeospaceLocation loc) {
		if (loc == null) return null;
		GeometryFactory gf = new GeometryFactory();
		Point pt = gf.createPoint(GeoUtils.toCoordinate(loc));
		for (Airspace a : _restricted) {
			if ((loc.getAltitude() < a._minAlt) || (loc.getAltitude() > a._maxAlt)) continue;
			if (a.contains(pt)) return a;
		}
		
		return null;
	}
	
	/**
	 * Returns whether any restricted airspace is within a set distance of a point.
	 * @param loc the GeoLocation
	 * @param distance the distance in miles
	 * @return a Collection of Airspace beans
	 */
	public static Collection<Airspace> findRestricted(GeoLocation loc, int distance) {
		GeometryFactory gf = new GeometryFactory();
		Point pt = gf.createPoint(GeoUtils.toCoordinate(loc));
		double dst = distance / GeoLocation.DEGREE_MILES;
		
		Collection<Airspace> results = new HashSet<Airspace>();
		_restricted.stream().filter(a -> (a.contains(pt) || (a._geo.distance(pt) < dst))).forEach(results::add);
		return results;
	}
	
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
	 * @see FIR#setBorder(Collection)
	 * @see FIR#addBorderPoint(GeoLocation)
	 */
	public List<GeoLocation> getBorder() {
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
	
	@Override
	public double getLatitude() {
		calculateGeo();
		return _geo.getCentroid().getX();
	}

	@Override
	public double getLongitude() {
		calculateGeo();
		return _geo.getCentroid().getY();
	}
	
	@Override
	public int getAltitude() {
		return (_maxAlt + _minAlt) / 2;
	}
	
	/**
	 * Returns whether a point is contained within this Airspace.
	 * @param loc a GeospaceLocation
	 * @return TRUE if the Airspace contains this point, otherwise FALSE
	 */
	public boolean contains(GeospaceLocation loc) {
		boolean isContained = (loc.getAltitude() >= _minAlt) && (loc.getAltitude() <= _maxAlt);
		if (isContained) {
			GeometryFactory gf = new GeometryFactory();
			Point pt = gf.createPoint(GeoUtils.toCoordinate(loc));
			isContained = contains(pt);
		}
		
		return isContained;
	}
	
	private boolean contains(Point p) {
		calculateGeo();
		return _geo.contains(p);
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
		_geo = null;
		_border.add(loc);
	}
	
	/**
	 * Sets the airspace boundary.
	 * @param locs a Collection of GeoLocations
	 * @see Airspace#getBorder()
	 */
	public void setBorder(Collection<GeoLocation> locs) {
		_geo = null;
		_border.addAll(locs);
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
	
	private void calculateGeo() {
		if (_geo == null)
			_geo = GeoUtils.toGeometry(_border);
	}
	
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"bld\">");
		buf.append(_id);
		buf.append("</span> - <span class=\"sec bld\">");
		buf.append(_type.getName());
		buf.append("</span><br />");
		buf.append(_name);
		buf.append("<br /><br /><span class=\"small\">From ");
		buf.append(_minAlt);
		buf.append(" MSL to ");
		buf.append(_maxAlt);
		buf.append(" MSL</span>");
		return buf.toString();
	}

	@Override
	public Object cacheKey() {
		return (_id + "!!" + _c.getCode());
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Airspace a2) ? (compareTo(a2) == 0) : false;
	}
	
	@Override
	public int hashCode() {
		return cacheKey().hashCode();
	}
	
	/**
	 * Compares two Airspaces by comparing their types and IDs.
	 */
	@Override
	public int compareTo(Airspace a2) {
		int tmpResult = _type.compareTo(a2._type);
		return (tmpResult == 0) ? _id.compareTo(a2._id) : tmpResult;
	}
	
	@Override
	public String toString() {
		return _id;
	}
}