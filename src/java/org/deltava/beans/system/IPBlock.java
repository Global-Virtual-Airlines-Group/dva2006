// Copyright 2009, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;
import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store IP address block information.
 * @author Luke
 * @version 8.7
 * @since 2.5
 */

public class IPBlock implements Cacheable, GeoLocation, Comparable<IPBlock> {
	
	private final GeoPosition _loc = new GeoPosition(0, 0);
	
	private final int _id;
	private final CIDRBlock _cidr;
	
	private Country _country;
	private String _region;
	private String _city;
	
	private int _radius;

	/**
	 * Initializes the bean.
	 * @param id the block ID
	 * @param cidr the CIDRBlock address
	 */
	public IPBlock(int id, String cidr) {
		super();
		_id = id;
		_cidr = new CIDRBlock(cidr);
	}

	/**
	 * Returns the base Address of the address block.
	 * @return the base IP address
	 */
	public String getAddress() {
		return _cidr.getNetworkAddress();
	}
	
	/**
	 * Returns the last Address of the address block.
	 * @return the last IP address
	 */
	public String getLastAddress() {
		return _cidr.getBroadcastAddress();
	}
	
	/**
	 * Returns the size of the address block.
	 * @return the size of the block in bits
	 */
	public int getBits() {
		return _cidr.getPrefixLength();
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
		return (1 << getType().getBits() - _cidr.getPrefixLength());
	}
	
	/**
	 * Returns the IP address type.
	 * @return an IPAddress
	 */
	public IPAddress getType() {
		return _cidr.isIPv6() ? IPAddress.IPV6 : IPAddress.IPV4;
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
	
	/**
	 * Returns the geolocation accuracy radius.
	 * @return the radius in miles
	 */
	public int getRadius() {
		return _radius;
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
	public boolean contains(String addr) {
		return _cidr.isInRange(addr);
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
	 * Sets the geolocaiton accuracy radius.
	 * @param radius the radius in miles 
	 */
	public void setRadius(int radius) {
		_radius = radius;
	}
	
	@Override
	public Object cacheKey() {
		return Long.valueOf(_id);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return _cidr.toString();
	}

	@Override
	public int compareTo(IPBlock o) {
		// TODO Auto-generated method stub
		return 0;
	}
}