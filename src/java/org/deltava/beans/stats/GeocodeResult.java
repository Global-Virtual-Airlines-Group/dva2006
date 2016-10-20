// Copyright 2008, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store Google Geocoding results. 
 * @author Luke
 * @version 7.2
 * @since 2.3
 */

public class GeocodeResult implements GeoLocation {
	
	private GeoLocation _loc;

	private String _address;
	private String _city;
	private String _state;
	private String _stateCode;
	private String _country;
	private String _countryCode;
	private String _postalCode;

	@Override
	public double getLatitude() {
		return _loc.getLatitude();
	}
	
	@Override
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	/**
	 * Returns the street address.
	 * @return the street address
	 * @see GeocodeResult#setAddress(String)
	 */
	public String getAddress() {
		return _address;
	}
	
	/**
	 * Returns the postal code.
	 * @return the postal code
	 * @see GeocodeResult#setPostalCode(String)
	 */
	public String getPostalCode() {
		return _postalCode;
	}
	
	/**
	 * Returns the city name.
	 * @return the city name
	 * @see GeocodeResult#setCity(String)
	 */
	public String getCity() {
		return _city;
	}
	
	/**
	 * Returns the state name.
	 * @return the state name
	 * @see GeocodeResult#setState(String)
	 */
	public String getState() {
		return _state;
	}
	
	/**
	 * Returns the state code.
	 * @return the state code
	 * @see GeocodeResult#setStateCode(String)
	 */
	public String getStateCode() {
		return _stateCode;
	}
	
	/**
	 * Returns the country name.
	 * @return the country name
	 * @see GeocodeResult#setCountry(String)
	 */
	public String getCountry() {
		return _country;
	}
	
	/**
	 * Returns the country code.
	 * @return the country code
	 * @see GeocodeResult#setCountryCode(String)
	 */
	public String getCountryCode() {
		return _countryCode;
	}

	/**
	 * Updates the street address.
	 * @param addr the street address
	 * @see GeocodeResult#getAddress()
	 */
	public void setAddress(String addr) {
		_address = addr;
	}
	
	/**
	 * Sets the postal code.
	 * @param code the postal code
	 * @see GeocodeResult#getPostalCode()
	 */
	public void setPostalCode(String code) {
		if (code != null)
			_postalCode = code.toUpperCase();
	}
	
	/**
	 * Sets the city name.
	 * @param city the city name
	 * @see GeocodeResult#getCity()
	 */
	public void setCity(String city) {
		_city = city;
	}
	
	/**
	 * Sets the state/province name.
	 * @param state the state/province name
	 * @see GeocodeResult#getState()
	 */
	public void setState(String state) {
		_state = state;
	}
	
	/**
	 * Sets the state/province code.
	 * @param code the state/province code
	 * @see GeocodeResult#getStateCode()
	 */
	public void setStateCode(String code) {
		if (code != null)
			_stateCode = code;
	}
	
	/**
	 * Sets the country name.
	 * @param country the country name
	 * @see GeocodeResult#getCountry()
	 */
	public void setCountry(String country) {
		_country = country;
	}
	
	/**
	 * Sets the country code.
	 * @param code the country code
	 * @see GeocodeResult#getCountryCode()
	 */
	public void setCountryCode(String code) {
		if (code != null)
			_countryCode = code.toUpperCase();
	}
	
	public void setLocation(GeoLocation loc) {
		_loc = new GeoPosition(loc);
	}

	/**
	 * Displays the city and state, and the country code if not US.
	 * @return the concatenated city, state and country
	 */
	public String getCityState() {
		StringBuilder buf = new StringBuilder();
		if (_city != null) {
			buf.append(_city);
			buf.append(", ");
		}
		
		boolean isUS ="US".equals(_countryCode);
		boolean hasStateCode = isUS || "CA".equals(_countryCode);
		if (hasStateCode && (_stateCode != null)) {
			buf.append(_stateCode);
			buf.append(' ');
		} else if ((_state != null) && (!_state.equals(_city))) {
			buf.append(_state);
			buf.append(' ');
		}
		
		if (!isUS && (_country != null)) {
			int total = buf.length() + _country.length();
			buf.append((total > 60) ? _countryCode : _country);
		}
		
		return buf.toString();
	}

	@Override
	public String toString() {
		return getCityState();
	}
}