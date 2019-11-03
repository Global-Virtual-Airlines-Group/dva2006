// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store IP block geolocation data.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public class IPLocation extends DatabaseBean {
	
	private Country _c;
	private String _region;
	private String _regionCode;
	private String _city;
	
	/**
	 * Creates a location bean.
	 * @param id the GeoNames ID
	 * 
	 */
	public IPLocation(int id) {
		super();
		setID(id);
	}

	/**
	 * Returns the country.
	 * @return the country
	 */
	public Country getCountry() {
		return _c;
	}
	
	/**
	 * Returns the region/state/province code.
	 * @return the code
	 */
	public String getRegionCode() {
		return _regionCode;
	}
	
	/**
	 * Returns the region/state/province name.
	 * @return the name
	 */
	public String getRegion() {
		return _region;
	}
	
	/**
	 * Returns the city name.
	 * @return the city
	 */
	public String getCityName() {
		return _city;
	}
	
	/**
	 * Updates the country.
	 * @param c a Country
	 */
	public void setCountry(Country c) {
		_c = c;
	}
	
	/**
	 * Updates the region code.
	 * @param code the code
	 */
	public void setRegionCode(String code) {
		_regionCode = code.toUpperCase();
	}
	
	/**
	 * Updates the region name.
	 * @param name the name
	 */
	public void setRegion(String name) {
		_region = name;
	}
	
	/**
	 * Updates the city name.
	 * @param name the city
	 */
	public void setCityName(String name) {
		_city = name;
	}
}