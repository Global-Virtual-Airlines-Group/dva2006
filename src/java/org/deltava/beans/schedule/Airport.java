// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2020, 2021 Globa Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.*;

/**
 * A class for storing airport information.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class Airport implements Comparable<Airport>, Auditable, ComboAlias, ViewEntry, ICAOAirport, MarkerMapEntry, IconMapEntry {

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
	private String _region;
	private String _supercededAirport;
	private boolean _isFictionalCode;
	private boolean _asdeX;
	private boolean _hasGateData;
	private boolean _isSchengen;
	private boolean _hasUSPFI;
	
	private final GeoPosition _position = new GeoPosition(0d, 0d);
	private TZInfo _tz = TZInfo.UTC;
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
	 * Updates the length of the longest runway at this Airport.
	 * @param len the length in feet
	 * @see Airport#getMaximumRunwayLength()
	 */
	public void setMaximumRunwayLength(int len) {
		_maxRunwayLength = Math.max(_maxRunwayLength, len);
	}
	
	/**
	 * Sets whether this airport has ASDE-X radar.
	 * @param hasASDE TRUE if ASDE-X present, otherwise FALSE
	 * @see Airport#getASDE()
	 */
	public void setASDE(boolean hasASDE) {
		_asdeX = hasASDE;
	}
	
	/**
	 * Sets whether this airport has gate data configured.
	 * @param hasData TRUE if gate data configured, otherwise FALSE
	 * @see Airport#getGateData()
	 */
	public void setGateData(boolean hasData) {
		_hasGateData = hasData;
	}
	
	/**
	 * Sets whether this Airport is in the Schengen Zone.
	 * @param isSZ TRUE if in the Schengen Zone, otherwise FALSE
	 * @see Airport#getIsSchengen()
	 */
	public void setIsSchengen(boolean isSZ) {
		_isSchengen = isSZ;
	}
	
	/**
	 * Sets whether this Airport has a US Pre-Flight Inspection station.
	 * @param hasPFI TRUE if a PFI present, otherwise FALSE
	 */
	public void setHasPFI(boolean hasPFI) {
		_hasUSPFI = hasPFI;
	}
	
	/**
	 * Sets whether this Airport is using a fictional IATA code, for non-extant Airports.
	 * @param isFictional TRUE if a fictional code, otherwise FALSE
	 */
	public void setHasFictionalCode(boolean isFictional) {
		_isFictionalCode = isFictional;
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
	 * Returns whether this Airport is in the Schengen Zone.
	 * @return TRUE if in the Schengen Zone, otherwise FALSE
	 * @see Airport#setIsSchengen(boolean)
	 */
	public boolean getIsSchengen() {
		return _isSchengen;
	}
	
	/**
	 * Returns whether this Airport has a US customs pre-flight inspection station.
	 * @return TRUE if PFI present, otherwise FALSE
	 * @see Airport#setHasPFI(boolean)
	 */
	public boolean getHasPFI() {
		return _hasUSPFI;
	}
	
	/**
	 * Returns whether the Airport has a fictional IATA code, for non-existent airports.
	 * @return TRUE if the code is fictional, otherwise FALSE
	 */
	public boolean getHasFictionalCode() {
		return _isFictionalCode;
	}

	@Override
	public final double getLatitude() {
		return _position.getLatitude();
	}

	@Override
	public final double getLongitude() {
		return _position.getLongitude();
	}
	
	/**
	 * Returns whether this Airport has ASDE-X radar.
	 * @return TRUE if ASDE-X radar present, otherwise FALSE
	 * @see Airport#setASDE(boolean)
	 */
	public boolean getASDE() {
		return _asdeX;
	}
	
	/**
	 * Returns whether this Airport has gate data configured
	 * @return TRUE if gate data configured, otherwise FALSE
	 * @see Airport#setGateData(boolean)
	 */
	public boolean getGateData() {
		return _hasGateData;
	}
	
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
		if (airlines != null)
			airlines.forEach(al -> addAirlineCode(al));
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
	
	@Override
   public String getIconColor() {
      return GREEN;
   }
   
   @Override
	public int getPaletteCode() {
		return 2;
	}
	
   @Override
	public int getIconCode() {
		return 48;
	}
   
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

	@Override
	public final int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String getRowClassName() {
		if (_aCodes.isEmpty())
			return "warn";
		
		return _hasGateData ? null : "opt1";
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append(" (").append(_iata).append(')');
		return buf.toString();
	}
	
	@Override
	public String getAuditID() {
		return _icao;
	}
	
	@Override
	public Object clone() {
		Airport a2 = new Airport(_iata, _icao, _name);
		a2._alt = _alt;
		a2._region = _region;
		a2._supercededAirport = _supercededAirport;
		a2._asdeX = _asdeX;
		a2._tz = _tz;
		a2._maxRunwayLength = _maxRunwayLength;
		a2._hasGateData = _hasGateData;
		a2._country = _country;
		a2._isSchengen = _isSchengen;
		a2._hasUSPFI = _hasUSPFI;
		a2.setLocation(_position.getLatitude(), _position.getLongitude());
		a2._aCodes.addAll(_aCodes);
		return a2;
	}
}