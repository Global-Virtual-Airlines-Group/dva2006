// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2015 Globa Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.*;

/**
 * A class for storing airport information.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class Airport implements java.io.Serializable, Comparable<Airport>, ComboAlias, ViewEntry, 
	ICAOAirport, MarkerMapEntry, IconMapEntry, Cloneable {

	/**
	 * Enumeration for Airport code types.
	 */
	public enum Code {
		IATA, ICAO;
	}
	
	/**
	 * Special airport object for "All Airports".
	 */
	public static final Airport ALL = new Airport("$AL", "$ALL", "All Airports") {
		@Override
		public String getComboName() {
			return getName();
		}
	};

	private String _iata;
	private String _icao;
	private String _name;
	private int _alt;
	private int _maxRunwayLength;
	private double _magVar;
	private String _region;
	private String _supercededAirport;
	private boolean _adseX;
	
	private final GeoPosition _position = new GeoPosition(0d, 0d);
	private TZInfo _tz = TZInfo.local();
	private Country _country;
	private final Collection<String> _aCodes = new TreeSet<String>();

	/**
	 * Create a new Airport object.
	 * @param iata The airport's IATA code
	 * @param icao The airport's ICAO code
	 * @param name The airport's name
	 * @throws NullPointerException If the IATA or ICAO codes are null
	 */
	public Airport(String iata, String icao, String name) {
		this(name);
		setIATA(iata);
		setICAO(icao);
	}

	/**
	 * Create a new Airport object.
	 * @param name The airport name
	 */
	public Airport(String name) {
		super();
		setName(name);
	}

	/**
	 * Sets this airport's location.
	 * @param lat This airport's latitude
	 * @param lng This airport's longitude
	 */
	public void setLocation(double lat, double lng) {
		_position.setLatitude(lat);
		_position.setLongitude(lng);
	}

	/**
	 * Set this airport's Time Zone.
	 * @param tz A Time Zone wrapper for the time zone
	 * @see Airport#getTZ()
	 */
	public void setTZ(TZInfo tz) {
		_tz = tz;
	}
	
	/**
	 * Sets this airport's Country.
	 * @param c the Country
	 * @see Airport#getCountry()
	 */
	public void setCountry(Country c) {
		_country = c;
	}

	/**
	 * Sets this airport's Time Zone.
	 * @param tzID A valid JVM time zone ID
	 */
	public void setTZ(String tzID) {
		_tz = TZInfo.get(tzID);
	}
	
	/**
	 * Sets this airport's magnetic variation.
	 * @param mv the magnetic variation in degrees
	 */
	public void setMagVar(double mv) {
		_magVar = mv;
	}
	
	/**
	 * Updates the length of the longest runway at this Airport.
	 * @param len the length in feet
	 * @see Airport#getMaximumRunwayLength()
	 */
	public void setMaximumRunwayLength(int len) {
		_maxRunwayLength = Math.max(_maxRunwayLength, len);
	}
	
	/**
	 * Sets whether this airport has ADSE-X radar.
	 * @param hasADSE TRUE if ADSE-X present, otherwise FALSE
	 */
	public void setADSE(boolean hasADSE) {
		_adseX = hasADSE;
	}

	/**
	 * Sets this airport's name.
	 * @param name the airport name
	 * @see Airport#getName()
	 */
	public void setName(String name) {
		_name = StringUtils.strip(name, ",");
	}

	/**
	 * Sets this Airport's ICAO code.
	 * @param code the ICAO code
	 * @throws NullPointerException if code is null
	 * @see Airport#getICAO()
	 */
	public void setICAO(String code) {
		_icao = code.trim().toUpperCase();
	}
	
	/**
	 * Sets this Airport's IATA code.
	 * @param code the IATA code
	 * @throws NullPointerException if code is null
	 * @see Airport#getIATA()
	 */
	public void setIATA(String code) {
		_iata = code.trim().toUpperCase();
	}
	
	/**
	 * Sets this airport's ICAO region.
	 * @param code the region code
	 * @see Airport#getRegion()
	 */
	public void setRegion(String code) {
		_region = (code == null) ? null : code.toUpperCase();
	}

	/**
	 * Sets this Airport's altitude.
	 * @param alt the altitude in feet MSL
	 * @see Airport#getAltitude()
	 */
	public void setAltitude(int alt) {
	   _alt = alt;
	}
	
	/**
	 * Updates the superceded airport code for this airport.
	 * @param iata the airport IATA code
	 * @see Airport#getSupercededAirport()
	 */
	public void setSupercededAirport(String iata) {
		_supercededAirport = (iata == null) ? null : iata.toUpperCase();
	}
	
	/**
	 * Return this airprort's IATA Code.
	 * @return The IATA code
	 */
	public String getIATA() {
		return _iata;
	}

	/**
	 * Return this airport's ICAO Code.
	 * @return The ICAO code
	 */
	@Override
	public String getICAO() {
		return _icao;
	}

	/**
	 * Return this airport's name.
	 * @return The Airport name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Return this airport's altitude.
	 * @return the altitude in feet MSL
	 */
	@Override
	public int getAltitude() {
	   return _alt;
	}
	
	/**
	 * Returns the ICAO region containing this Airport.
	 * @return the region code
	 */
	public String getRegion() {
		return _region;
	}
	
	/**
	 * Returns this airport's time zone.
	 * @return the Airport's time zone
	 */
	public TZInfo getTZ() {
		return _tz;
	}
	
	/**
	 * Returns this airport's country.
	 * @return the Country
	 */
	public Country getCountry() {
		return _country;
	}
	
	/**
	 * Returns the State this airport is located in.
	 * @return a US State, or null
	 */
	public State getState() {
		if ("US".equals(_country.getCode())) {
			String state = _name.substring(_name.lastIndexOf(' ') + 1);
			if (state.length() == 2) {
				try {
					return State.valueOf(state);	
				} catch (Exception e) {
					// empty
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns this airport's magnetic variation.
	 * @return the magnetic variation in degrees 
	 */
	@Override
	public double getMagVar() {
		return _magVar;
	}
	
	/**
	 * Returns the maximum length of all runways at this airport.
	 * @return the length in feet of the longest runway
	 * @see Airport#setMaximumRunwayLength(int)
	 */
	public int getMaximumRunwayLength() {
		return _maxRunwayLength; 
	}
	
	/**
	 * Returns the IATA code of any former Airports replaced by this Airport.
	 * @return the IATA code, or null if none
	 * @see Airport#setSupercededAirport(String)
	 */
	public String getSupercededAirport() {
		return _supercededAirport;
	}

	/**
	 * Returns this airport's latitude and longitude.
	 * @return a GeoPosition object containing the airport's position
	 */
	public GeoPosition getPosition() {
		return _position;
	}

	/**
	 * Return this airport's latitude.
	 * @return this airport's latitude in degrees (and some fraction thereof)
	 * @see GeoPosition#getLatitude()
	 */
	@Override
	public final double getLatitude() {
		return _position.getLatitude();
	}

	/**
	 * Return this airport's longitude.
	 * @return This airport's longitude in degrees (and some fraction thereof)
	 * @see GeoPosition#getLongitude()
	 */
	@Override
	public final double getLongitude() {
		return _position.getLongitude();
	}
	
	/**
	 * Returns whether this Airport has ADSE-X radar.
	 * @return TRUE if ADSE-X radar present, otherwise FALSE
	 * @see Airport#setADSE(boolean)
	 */
	public boolean getADSE() {
		return _adseX;
	}
	
	/**
	 * Sort the airports by comparing their IATA codes.
	 */
	@Override
	public int compareTo(Airport a2) {
		int tmpResult = _iata.compareTo(a2._iata);
		return (tmpResult == 0) ? _icao.compareTo(a2._icao) : tmpResult;
	}

	@Override
	public String getComboAlias() {
		return getIATA();
	}

	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder(getName());
		buf.append(" (");
		buf.append(getIATA());
		buf.append(')');
		return buf.toString();
	}

	/**
	 * Adds an airline to the list of airlines associated with this Airport.
	 * @param aCode The airline code
	 * @throws NullPointerException if aCode is null
	 * @see Airport#removeAirlineCode(String)
	 * @see Airport#setAirlines(Collection)
	 * @see Airline#getCode()
	 */
	public void addAirlineCode(String aCode) {
		_aCodes.add(aCode.trim().toUpperCase());
	}
	
	/**
	 * Removes an airline from the list of airlines associated with this Airport.
	 * @param aCode The airline code
	 * @throws NullPointerException if aCode is null
	 * @see Airport#addAirlineCode(String)
	 * @see Airport#setAirlines(Collection)
	 * @see Airline#getCode()
	 */
	public void removeAirlineCode(String aCode) {
		_aCodes.remove(aCode.trim().toUpperCase());
	}

	/**
	 * Resets the list of Airlines associated with this Airport.
	 * @param airlines a Collection of Airline codes
	 * @see Airport#addAirlineCode(String)
	 * @see Airport#getAirlineCodes()
	 */
	public void setAirlines(Collection<String> airlines) {
		_aCodes.clear();
		if (airlines != null) {
			for (Iterator<String> i = airlines.iterator(); i.hasNext(); )
				addAirlineCode(i.next());
		}
	}

	/**
	 * Returns a list codes for the airlines associated with this Airport.
	 * @return The unsorted list of airline codes
	 * @see Airline#getCode()
	 */
	public Collection<String> getAirlineCodes() {
		return _aCodes;
	}

	/**
	 * Determines if a particular airline services this Airport.
	 * @param code The airline code
	 * @return If the airline is associated with this airport
	 * @see Airline#getCode()
	 */
	public boolean hasAirlineCode(String code) {
		return _aCodes.contains(code);
	}
	
	/**
	 * Returns if the position of this Airport has been set.
	 * @return TRUE if the position has been set, otherwise FALSE
	 */
	public boolean hasPosition() {
		return GeoUtils.isValid(_position);
	}
	
   /**
    * Return the default Google Maps icon color.
    * @return org.deltava.beans.MapEntry.GREEN
    */
	@Override
   public String getIconColor() {
      return GREEN;
   }
   
	/**
	 * Returns the Google Earth palette code.
	 * @return 2
	 */
   @Override
	public int getPaletteCode() {
		return 2;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 48
	 */
   @Override
	public int getIconCode() {
		return 48;
	}
   
   /**
    * Returns the default Google Maps infobox text.
    * @return an HTML String
    */
	@Override
   public String getInfoBox() {
      StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\"><span class=\"bld\">");
      buf.append(_name);
      buf.append("</span><br /><br />IATA Code: ");
      buf.append(_iata);
      buf.append("<br />ICAO Code: ");
      buf.append(_icao);
      if (_region != null) {
    	  buf.append("<br />ICAO Region: ");
    	  buf.append(_region);
      }
      
      buf.append("<br /><br />Latitude: ");
      buf.append(StringUtils.format(_position, true, GeoLocation.LATITUDE));
      buf.append("<br />Longitude: ");
      buf.append(StringUtils.format(_position, true, GeoLocation.LONGITUDE));
      buf.append("</div>");
      return buf.toString();
   }

	/**
	 * Compares airports by ensuring that both the IATA and ICAO code are the same. This leaves the possibility open of
	 * airports having the same IATA code but different ICAO codes.
	 */
	@Override
	public boolean equals(Object o2) {
		if (o2 instanceof Airport) {
			Airport a2 = (Airport) o2;
			return (_iata.equals(a2._iata) && _icao.equals(a2._icao));
		} else if (o2 instanceof String)
			return _iata.equals(o2) || _icao.equals(o2);
		else
			return false;
	}

	/**
	 * Retrurns the hashcode of the IATA/ICAO values.
	 */
	@Override
	public final int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String getRowClassName() {
		return _aCodes.isEmpty() ? "warn" : null;
	}

	/**
	 * Displays the airport name and IATA code.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append(" (").append(_iata).append(')');
		return buf.toString();
	}
	
	/**
	 * Clones the Airport object.
	 */
	@Override
	public Object clone() {
		Airport a2 = new Airport(_iata, _icao, _name);
		a2._alt = _alt;
		a2._magVar = _magVar;
		a2._region = _region;
		a2._supercededAirport = _supercededAirport;
		a2._adseX = _adseX;
		a2._tz = _tz;
		a2._maxRunwayLength = _maxRunwayLength;
		a2._country = _country;
		a2.setLocation(_position.getLatitude(), _position.getLongitude());
		a2._aCodes.addAll(_aCodes);
		return a2;
	}
}