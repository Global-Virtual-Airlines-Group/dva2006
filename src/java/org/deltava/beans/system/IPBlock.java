// Copyright 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;
import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store IP address block information.
 * @author Luke
 * @version 7.0
 * @since 2.5
 */

public abstract class IPBlock implements Cacheable, GeoLocation, Comparable<IPBlock> {
	
	private final GeoPosition _loc = new GeoPosition(0, 0);
	
	private final int _id;
	private final String _baseAddr;
	private final String _endAddr;
	private final int _bits;
	
	private Country _country;
	private String _region;
	private String _city;

	/**
	 * Initializes the bean.
	 * @param id the block ID
	 * @param start the start IP address
	 * @param end the ending IP address
	 * @param size the CIDR size in bits
	 */
	protected IPBlock(int id, String start, String end, int size) {
		super();
		_id = id;
		_baseAddr = start;
		_endAddr = end;
		_bits = size;
	}

	/**
	 * Returns the base Address of the address block.
	 * @return the base IP address
	 */
	public String getAddress() {
		return _baseAddr;
	}
	
	/**
	 * Returns the last Address of the address block.
	 * @return the last IP address
	 */
	public String getLastAddress() {
		return _endAddr;
	}
	
	/**
	 * Returns the size of the address block.
	 * @return the size of the block in bits
	 */
	public int getBits() {
		return _bits;
	}
	
	/**
	 * Returns the block database ID.
	 * @return the ID
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the size of the block.
	 * @return the number of addresses in the block
	 */
	public int getSize() {
		return 1 << (getType().getBits() - _bits);
	}
	
	/**
	 * Returns the IP address type.
	 * @return an IPAddress
	 */
	public abstract IPAddress getType();
	
	/**
	 * Returns the country associated with this IP address.
	 * @return the Country
	 * @see IPBlock#setCountry(Country)
	 */
	public Country getCountry() {
		return _country;
	}
	
	/**
	 * Returns the region/state associated with this IP address.
	 * @return the region name
	 * @see IPBlock#setRegion(String)
	 */
	public String getRegion() {
		return _region;
	}
	
	/**
	 * Returns the city associated with this IP address.
	 * @return the city name
	 * @see IPBlock#setCity(String)
	 */
	public String getCity() {
		return _city;
	}
	
	/**
	 * Returns the city, region and country (if available).
	 * @return the location
	 */
	public String getLocation() {
		StringBuilder buf = new StringBuilder(32);
		if (!StringUtils.isEmpty(_city))
			buf.append(_city).append(", ");
		if (!StringUtils.isEmpty(_region))
			buf.append(_region).append(' ');
		if (_country != null)
			buf.append(_country.getCode());
		
		return buf.toString();
	}
	
	@Override
	public double getLatitude() {
		return _loc.getLatitude();
	}

	@Override
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	/**
	 * Checks whether this IP block contains a specific IP address.
	 * @param addr the IP address
	 * @return TRUE if the block contains the address, otherwise FALSE
	 */
	public abstract boolean contains(String addr);

	/**
	 * Updates the country associated with this address block.
	 * @param c the Country
	 * @see IPBlock#getCountry()
	 */
	public void setCountry(Country c) {
		_country = c;
	}
	
	/**
	 * Updates the region/state associated with this address block.
	 * @param rgn the region name
	 * @see IPBlock#getRegion()
	 */
	public void setRegion(String rgn) {
		_region = rgn;
	}
	
	/**
	 * Updates the city associated with this address block.
	 * @param city the city name
	 * @see IPBlock#getCity()
	 */
	public void setCity(String city) {
		_city = city;
	}
	
	/**
	 * Updates the location of this address block.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 */
	public void setLocation(double lat, double lng) {
		_loc.setLatitude(lat);
		_loc.setLongitude(lng);
	}
	
	@Override
	public Object cacheKey() {
		return Long.valueOf(_id);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}