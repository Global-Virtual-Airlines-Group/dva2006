// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store IP address geo-location data. 
 * @author Luke
 * @version 3.4
 * @since 2.5
 */

public class IPAddressInfo implements java.io.Serializable, Cacheable, GeoLocation {
	
	private final GeoPosition _loc = new GeoPosition(0, 0);
	
	private String _addr;
	
	private IPBlock _cidr;
	
	private Country _country;
	private String _region;
	private String _city;
	
	/**
	 * Special IP Address info block for unknown addresses.
	 */
	public static final IPAddressInfo UNKNOWN = new IPAddressInfo("0.0.0.0") {
		{
			setCountry(Country.get("US"));
			setBlock(new IPBlock("0.0.0.0/8"));
		}
	};

	/**
	 * Initializes the bean. The address will be converted into a class C network
	 * address.
	 * @param addr the IP address
	 */
	public IPAddressInfo(String addr) {
		super();
		long rawAddr = NetworkUtils.pack(addr) & 0xFFFFFF00;
		_addr = NetworkUtils.format(NetworkUtils.convertIP(rawAddr));
	}

	/**
	 * Returns the IP address, converted to a Class C network address.
	 * @return the IP address
	 */
	public String getAddress() {
		return _addr;
	}
	
	/**
	 * Returns information about this address' network.
	 * @return an IPBlock bean with network information
	 * @see IPAddressInfo#setBlock(IPBlock)
	 */
	public IPBlock getBlock() {
		return _cidr;
	}
	
	/**
	 * Returns the country associated with this IP address.
	 * @return the Country
	 * @see IPAddressInfo#setCountry(Country)
	 */
	public Country getCountry() {
		return _country;
	}
	
	/**
	 * Returns the region/state associated with this IP address.
	 * @return the region name
	 * @see IPAddressInfo#setRegion(String)
	 */
	public String getRegion() {
		return _region;
	}
	
	/**
	 * Returns the city associated with this IP address.
	 * @return the city name
	 * @see IPAddressInfo#setCity(String)
	 */
	public String getCity() {
		return _city;
	}
	
	/**
	 * Returns the city, region and country (if available).
	 * @return the location
	 */
	public String getLocation() {
		StringBuilder buf = new StringBuilder(_city);
		if (buf.length() > 0)
			buf.append(", ");
		if (!StringUtils.isEmpty(_region)) {
			buf.append(_region);
			buf.append(' ');
		}
		
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
	 * Updates the country associated with this IP address.
	 * @param c the Country
	 * @see IPAddressInfo#getCountry()
	 */
	public void setCountry(Country c) {
		_country = c;
	}
	
	/**
	 * Updates the region/state associated with the IP address.
	 * @param rgn the region name
	 * @see IPAddressInfo#getRegion()
	 */
	public void setRegion(String rgn) {
		_region = rgn;
	}
	
	/**
	 * Updates the city associated with the IP address.
	 * @param city the city name
	 * @see IPAddressInfo#getCity()
	 */
	public void setCity(String city) {
		_city = city;
	}
	
	/**
	 * Updates information about this IP address' network.
	 * @param ipb an IPBlock bean
	 * @see IPAddressInfo#getBlock()
	 */
	public void setBlock(IPBlock ipb) {
		_cidr = ipb;
	}
	
	/**
	 * Updates the location of the IP address.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @throws NullPointerException if loc is null
	 */
	public void setLocation(double lat, double lng) {
		_loc.setLatitude(lat);
		_loc.setLongitude(lng);
	}
	
	public int hashCode() {
		return _addr.hashCode();
	}
	
	public String toString() {
		return _addr;
	}
	
	public Object cacheKey() {
		return _addr;
	}
}