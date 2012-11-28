// Copyright 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store IP address block information.
 * @author Luke
 * @version 5.0
 * @since 2.5
 */

public class IPBlock implements Cacheable, GeoLocation, Comparable<IPBlock> {
	
	private final GeoPosition _loc = new GeoPosition(0, 0);
	
	private final int _id;
	private final String _baseAddr;
	private final String _endAddr;
	private final long _rawAddr;
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
	public IPBlock(int id, String start, String end, int size) {
		super();
		_id = id;
		_baseAddr = start;
		_endAddr = end;
		_bits = size;
		_rawAddr = NetworkUtils.pack(start);
	}

	/**
	 * Returns the base Address of the address block.
	 * @return the base IP address
	 */
	public String getAddress() {
		return _baseAddr;
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
		return 1 << (32-_bits);
	}
	
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
	
	public double getLatitude() {
		return _loc.getLatitude();
	}
	
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	/**
	 * Checks whether this IP block contains a specific IP address.
	 * @param addr the IP address
	 * @return TRUE if the block contains the address, otherwise FALSE
	 */
	public boolean contains(String addr) {
		long intAddr = NetworkUtils.pack(addr);
		return (intAddr >= _rawAddr) && (intAddr <= NetworkUtils.pack(_endAddr));
	}

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
	
	/**
	 * Compares two IP Ranges by comparing their base addresses and mask sizes.
	 */
	public int compareTo(IPBlock ib2) {
		int tmpResult = Long.valueOf(_rawAddr).compareTo(Long.valueOf(ib2._rawAddr));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_bits).compareTo(Integer.valueOf(ib2._bits));
		
		return tmpResult;
	}
	
	public Object cacheKey() {
		return Integer.valueOf(_id);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(_baseAddr);
		buf.append('/').append(_bits);
		return buf.toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
}