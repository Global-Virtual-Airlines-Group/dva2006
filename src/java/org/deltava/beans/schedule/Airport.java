package org.deltava.beans.schedule;

import java.io.Serializable;
import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;
import org.deltava.beans.TZInfo;
import org.deltava.util.StringUtils;

/**
 * A class for storing airport information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Airport implements Serializable, Comparable, ComboAlias, GeoLocation, MapEntry {

	public static final int IATA = 0;
	public static final int ICAO = 1;

	/**
	 * Airport Code types.
	 */
	public static final String[] CODETYPES = { "IATA", "ICAO" };

	/**
	 * Special airport object for "All Airports"
	 */
	public static final Airport ALL = new Airport("$AL", "$ALL", "All Airports") {
		public String getComboName() {
			return getName();
		}
	};

	private String _iata;
	private String _icao;
	private String _name;
	private GeoPosition _position;
	private TZInfo _tz = TZInfo.local();
	private Set _aCodes = new TreeSet();

	/**
	 * Create a new Airport object.
	 * @param iata The airport's IATA code
	 * @param icao The airport's ICAO code
	 * @param name The airport's name
	 * @throws NullPointerException If the IATA or ICAO codes are null
	 */
	public Airport(String iata, String icao, String name) {
		this(name);
		_iata = iata.toUpperCase();
		setICAO(icao);
	}

	/**
	 * Create a new Airport object.
	 * @param name The airport name
	 */
	public Airport(String name) {
		super();
		setName(name);
		_position = new GeoPosition(0f, 0f);
	}

	/**
	 * Sets this airport's location.
	 * @param lat This airport's latitude
	 * @param lng This airport's longitude
	 * @see GeoPosition#GeoPosition(double, double)
	 */
	public void setLocation(double lat, double lng) {
		_position = new GeoPosition(lat, lng);
	}

	/**
	 * Set this airport's Time Zone.
	 * @param tz A Time Zone wrapper for the time zone
	 */
	public void setTZ(TZInfo tz) {
		_tz = tz;
	}

	/**
	 * Sets this airport's Time Zone.
	 * @param tzID A valid JVM time zone ID
	 */
	public void setTZ(String tzID) {
		_tz = TZInfo.get(tzID);
	}

	/**
	 * Sets this airport's name.
	 * @param name the airport name
	 * @see Airport#getName()
	 */
	public void setName(String name) {
		_name = name;
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
	 * Return this airport's time zone.
	 * @return The Airport's time zone
	 */
	public TZInfo getTZ() {
		return _tz;
	}

	/**
	 * Returns this airport's latitude and longitude.
	 * @return A GeoPosition object containing the airport's position
	 */
	public GeoPosition getPosition() {
		return _position;
	}

	/**
	 * Return this airport's latitude.
	 * @return This airport's latitude in degrees (and some fraction thereof)
	 * @see GeoPosition#getLatitude()
	 */
	public final double getLatitude() {
		return _position.getLatitude();
	}

	/**
	 * Return this airport's longitude.
	 * @return This airport's longitude in degrees (and some fraction thereof)
	 * @see GeoPosition#getLongitude()
	 */
	public final double getLongitude() {
		return _position.getLongitude();
	}
	
	/**
	 * Returns the hemispheres containing this Airport.
	 * @return bit-wise hemisphere constants
	 * @see GeoPosition#getHemisphere()
	 */
	public final int getHemisphere() {
	   return _position.getHemisphere();
	}

	/**
	 * Sort the airports by comparing their codes. <i>This assumes that both are set to display the same code, ICAO or
	 * IATA. </i>
	 */
	public int compareTo(Object o2) {
		Airport a2 = (Airport) o2;
		return getIATA().compareTo(a2.getIATA());
	}

	public String getComboAlias() {
		return getIATA();
	}

	public String getComboName() {
		StringBuffer buf = new StringBuffer(getName());
		buf.append(" (");
		buf.append(getIATA());
		buf.append(')');
		return buf.toString();
	}

	/**
	 * Adds an airline to the list of airlines associated with this Airport.
	 * @param aCode The airline code
	 * @throws NullPointerException if aCode is null
	 * @see Airport#setAirlines(Collection)
	 * @see Airline#getCode()
	 */
	public void addAirlineCode(String aCode) {
		_aCodes.add(aCode.trim().toUpperCase());
	}

	/**
	 * Resets the list of Airlines associated with this Airport.
	 * @param airlines a Collection of Airline codes
	 * @see Airport#addAirlineCode(String)
	 * @see Airport#getAirlineCodes()
	 */
	public void setAirlines(Collection airlines) {
		_aCodes.clear();
		for (Iterator i = airlines.iterator(); i.hasNext(); )
		   addAirlineCode((String) i.next());
	}

	/**
	 * Returns a list codes for the airlines associated with this Airport.
	 * @return The unsorted list of airline codes
	 * @see Airline#getCode()
	 */
	public Collection getAirlineCodes() {
		return new HashSet(_aCodes);
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
		return (_position != null) && (_position.getLatitude() != 0) && (_position.getLongitude() != 0.0);
	}
	
   /**
    * Return the default Google Maps icon color.
    * @return org.deltava.beans.MapEntry.GREEN
    */
   public String getIconColor() {
      return GREEN;
   }
   
   /**
    * Returns the default Google Maps infobox text.
    * @return an HTML String
    */
   public String getInfoBox() {
      StringBuffer buf = new StringBuffer("<b>");
      buf.append(_name);
      buf.append("</b><br /><span class=\"small\"><br />IATA Code: ");
      buf.append(_iata);
      buf.append("<br />ICAO Code: ");
      buf.append(_icao);
      buf.append("<br /><br />Latitude: ");
      buf.append(StringUtils.format(_position, true, GeoLocation.LATITUDE));
      buf.append("<br />Longitude: ");
      buf.append(StringUtils.format(_position, true, GeoLocation.LONGITUDE));
      buf.append("</span>");
      return buf.toString();
   }

	/**
	 * Compares airports by ensuring that both the IATA and ICAO code are the same. This leaves the possibility open of
	 * airports having the same IATA code but different ICAO codes.
	 */
	public boolean equals(Object o2) {
		try {
			Airport a2 = (Airport) o2;
			return (_iata.equals(a2.getIATA()) && _icao.equals(a2.getICAO()));
		} catch (ClassCastException cce) {
			return false;
		}
	}

	/**
	 * Retrurns the hashcode of the IATA/ICAO values.
	 */
	public final int hashCode() {
		return (_iata + _icao).hashCode();
	}

	/**
	 * Displays the airport name and IATA code.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer(_name);
		buf.append(" (");
		buf.append(_iata);
		buf.append(")");
		return buf.toString();
	}
}