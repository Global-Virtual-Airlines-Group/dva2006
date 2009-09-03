// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store Google Geocoding results. 
 * @author Luke
 * @version 2.6
 * @since 2.3
 */

public class GeocodeResult implements GeoLocation, Comparable<GeocodeResult> {
	
	public enum GeocodeAccuracy implements Comparable<GeocodeAccuracy> {
		UNKNOWN(0), COUNTRY(1), STATE(2), COUNTY(3), TOWN(4), POSTALCODE(5),
			STREET(6), INTERSECTION(7), ADDRESS(8), BUILDING(9);

		private int _value;
		
		GeocodeAccuracy(int value) {
			_value = value;
		}
		
		public int intValue() {
			return _value;
		}
	}
	
	private GeoLocation _loc;
	private GeocodeAccuracy _precision;

	private String _address;
	private String _city;
	private String _state;
	private String _country;
	private String _countryCode;
	private String _postalCode;

	/**
	 * Initializes the result.
	 * @param loc the location coordinates
	 * @param precision the accuracy of the result
	 */
	public GeocodeResult(GeoLocation loc, GeocodeAccuracy precision) {
		super();
		_loc = new GeoPosition(loc);
		_precision = precision;
	}

	public double getLatitude() {
		return _loc.getLatitude();
	}
	
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	/**
	 * Returns the accuracy of the resut.
	 * @return the accuracy of the result
	 */
	public GeocodeAccuracy getAccuracy() {
		return _precision;
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
	 * Returns the state code.
	 * @return the state code
	 * @see GeocodeResult#setState(String)
	 */
	public String getState() {
		return _state;
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

	/**
	 * Compares two results by comparing their accuracy.
	 */
	public int compareTo(GeocodeResult r2) {
		return _precision.compareTo(r2._precision);
	}
	
	/**
	 * Displays the city and state, and the country code if not US.
	 */
	public String getCityState() {
		StringBuilder buf = new StringBuilder();
		if (_city != null) {
			buf.append(_city);
			buf.append(", ");
		}
		
		if ((_state != null) && (!_state.equals(_city))) {
			buf.append(_state);
			buf.append(' ');
		}
		
		buf.append((_country.length() > 8) ? _countryCode : _country);
		return buf.toString();
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(getCityState());
		buf.append('-');
		buf.append(_precision.toString());
		buf.append('(');
		buf.append(_precision.intValue());
		buf.append(')');
		return buf.toString();
	}
}