// Copyright 2005, 2007, 2008, 2009, 2010, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store common properties for Navigation Database objects.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public abstract class NavigationDataBean implements Cloneable, Cacheable, Comparable<NavigationDataBean>, MarkerMapEntry, IconMapEntry {

	private static final long serialVersionUID = 4880772877749063792L;
	
	private String _code;
	private String _name;
	private String _region;
	private String _airway;
	private final Navaid _type;

	private final GeoPosition _gp;

	/**
	 * Creates a new Navigation Object.
	 * @param type the object type code
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 * @see NavigationDataBean#getLatitude()
	 * @see NavigationDataBean#getLongitude()
	 */
	public NavigationDataBean(Navaid type, double lat, double lon) {
		super();
		_type = type;
		_gp = new GeoPosition(lat, lon);
	}

	/**
	 * Returns the object's code.
	 * @return the navigation object code
	 * @see NavigationDataBean#setCode(String)
	 */
	public String getCode() {
		return _code;
	}

	/**
	 * Returns the object's name.
	 * @return the name
	 * @see NavigationDataBean#setName(String)
	 */
	public String getName() {
		return _name;
	}

	@Override
	public final double getLatitude() {
		return _gp.getLatitude();
	}

	@Override
	public final double getLongitude() {
		return _gp.getLongitude();
	}

	/**
	 * Returns the distance from this point to another point.
	 * @param loc the point
	 * @return the distance in miles
	 */
	public final int distanceTo(GeoLocation loc) {
		return _gp.distanceTo(loc);
	}

	/**
	 * Returns the object's position.
	 * @return the GeoPosition of this object
	 * @see NavigationDataBean#getLatitude()
	 * @see NavigationDataBean#getLongitude()
	 */
	public GeoPosition getPosition() {
		return _gp;
	}

	/**
	 * Returns the object's type.
	 * @return the object type code
	 */
	public final Navaid getType() {
		return _type;
	}

	/**
	 * Returns the ICAO region code for this entry.
	 * @return the region code
	 * @see NavigationDataBean#setRegion(String)
	 */
	public final String getRegion() {
		return _region;
	}
	
	/**
	 * Returns if this waypoint is on an Airway. 
	 * @return the airway code
	 */
	public String getAirway() {
		return _airway;
	}
	
	/**
	 * Returns whether this waypoint is part of a terminal route.
	 * @return TRUE if part of a Terminal Route, otherwise FALSE
	 */
	public boolean isInTerminalRoute() {
		return (_airway != null) && _airway.contains(".");
	}

	/**
	 * Updates the object's code.
	 * @param code the code
	 * @see NavigationDataBean#getCode()
	 */
	public void setCode(String code) {
		if (code != null)
			_code = code.trim().toUpperCase();
	}

	/**
	 * Updates the object's name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see NavigationDataBean#setName(String)
	 */
	public void setName(String name) {
		_name = name.replace(',', ' ').trim();
	}
	
	/**
	 * Updates the ICAO region code for this entry.
	 * @param rCode the ICAO region code
	 * @see NavigationDataBean#getRegion()
	 */
	public void setRegion(String rCode) {
		_region = (rCode == null) ? null : rCode.toUpperCase();
	}
	
	/**
	 * Updates if this entry is on an airway.
	 * @param code the airway code, or null if none
	 */
	public void setAirway(String code) {
		_airway = (code == null) ? null : code.toUpperCase();
	}

	/**
	 * Compares two objects by comparing their codes. If the codes are equal, then their item types and then 
	 * distances from point 0,0 are compared.
	 */
	@Override
	public int compareTo(NavigationDataBean nb2) {
		int tmpResult = _code.compareTo(nb2._code);
		if (tmpResult == 0) tmpResult = _type.compareTo(nb2._type);
		if (tmpResult == 0) {
			GeoPosition gp = new GeoPosition(0, 0);
			int d1 = gp.distanceTo(this);
			int d2 = gp.distanceTo(nb2);
			tmpResult = Integer.compare(d1, d2);
		}

		return tmpResult;
	}

	/**
	 * Helper method to return the item type and code for HTML infoboxes.
	 * @return the HTML-formatted code and description
	 * @see MapEntry#getInfoBox()
	 */
	protected String getHTMLTitle() {
		StringBuilder buf = new StringBuilder("<span class=\"bld\">");
		buf.append(getCode());
		buf.append("</span>");
		if ((_type != Navaid.INT) && (!StringUtils.isEmpty(_name))) {
			buf.append(' ');
			buf.append(_name);
		}
			
		buf.append(" (");
		buf.append(_type.getName());
		buf.append(")<br />");
		if (_region != null) {
			buf.append("Region: ");
			buf.append(_region);
			buf.append("<br />");
		}
		if (_airway != null) {
			buf.append("Airway: ");
			buf.append(_airway);
			buf.append("<br />");
		}
		
		buf.append("<br />");
		return buf.toString();
	}

	/**
	 * Helper method to return the Latitude/Longitude for HTML infoboxes.
	 * @return the HTML-formatted latitude/longitude
	 * @see MapEntry#getInfoBox()
	 */
	protected String getHTMLPosition() {
		StringBuilder buf = new StringBuilder("Latitude: ");
		buf.append(StringUtils.format(_gp, true, GeoLocation.LATITUDE));
		buf.append("<br />Longitude: ");
		buf.append(StringUtils.format(_gp, true, GeoLocation.LONGITUDE));
		buf.append("<br />");
		return buf.toString();
	}

	/**
	 * Returns the code's hash code.
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Checks for equality by comparing the codes.
	 */
	@Override
	public boolean equals(Object o2) {
		return (o2 instanceof NavigationDataBean) ? (compareTo((NavigationDataBean) o2) == 0) : false;
	}

	@Override
	public Object cacheKey() {
		return getCode();
	}
	
	/**
	 * Returns this navigation aid's unique ID in a format that matches the ACARS dispatch client. <i>Changing
	 * this format will probably break the dispatch client!</i>
	 */
	@Override
	public final String toString() {
		StringBuilder buf = new StringBuilder(_code);
		buf.append('!');
		buf.append(_type.ordinal());
		buf.append('!');
		buf.append(_gp.getLatitude());
		buf.append('!');
		buf.append(_gp.getLongitude());
		if (_region != null) {
			buf.append('!');
			buf.append(_region);
		}
		
		return buf.toString();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Creates a bean from a type, latitude and longitude.
	 * @param type the navigation aid type
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @return a NavigationDataBean, or null if the type is unknown
	 */
	public static NavigationDataBean create(Navaid type, double lat, double lng) {
		switch (type) {
		case VOR:
			return new VOR(lat, lng);
			
		case NDB:
			return new NDB(lat, lng);
			
		case INT:
			return new Intersection(null, lat, lng);
			
		case RUNWAY:
			return new Runway(lat, lng);
			
		case AIRPORT:
			return new AirportLocation(lat, lng);
			
		case GATE:
			return new Gate(lat, lng);

		default:
			return null;
		}
	}
	
	/**
	 * Creats a bean from a unique ID that matches the ACARS dispatch client.
	 * @param id the ID.
	 * @return a NavigationDataBean
	 */
	public static NavigationDataBean create(String id) {
		StringTokenizer tkns = new StringTokenizer(id, "!");
		String code = tkns.nextToken();
		Navaid nt = Navaid.values()[StringUtils.parse(tkns.nextToken(), Navaid.INT.ordinal())];
		NavigationDataBean nd = create(nt, StringUtils.parse(tkns.nextToken(), 0.0), StringUtils.parse(tkns.nextToken(), 0.0));
		nd.setCode(code);
		if (tkns.hasMoreTokens())
			nd.setRegion(tkns.nextToken());
			
		return nd;
	}
	
	/**
	 * Returns whether a waypoint code is actually encoded coordinates.
	 * @param code the waypoint code
	 * @return TRUE if in XXYYN format or XXNYYYE format, otherwise FALSE
	 */
	public static CodeType isCoordinates(String code) {
		if (StringUtils.isEmpty(code))
			return CodeType.CODE;
		if (!Character.isDigit(code.charAt(0)))
			return CodeType.CODE;
		if (!Character.isLetter(code.charAt(code.length() - 1)))
			return CodeType.CODE;
		
		// Check to ensure no additional letters, except in position 1/2
		int ltrCount = 0;
		for (int x = 1; x < code.length() - 2; x++) {
			if (Character.isLetter(code.charAt(x))) {
				ltrCount++;
				if ((ltrCount > 1) || (x > 2))
					return CodeType.CODE;
			}
		}
		
		return (ltrCount == 0) ? CodeType.QUADRANT : CodeType.FULL;
	}
}