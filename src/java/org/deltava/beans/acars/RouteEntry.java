// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;
import java.io.Serializable;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.StringUtils;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RouteEntry implements Comparable, GeoLocation, Serializable, MapEntry, ACARSFlags {

	private Date _date;
	private GeoPosition _gpos;

	private int _alt;
	private int _hdg;
	private int _aSpeed;
	private int _gSpeed;
	private int _vSpeed;
	private double _n1;
	private double _n2;
	private double _mach;
	private int _flaps;
	private int _flags;

	private static final int[] AP_FLAGS = { FLAG_AP_APR, FLAG_AP_HDG, FLAG_AP_NAV, FLAG_AP_ALT };
	private static final String[] AP_FLAG_NAMES = { "APR", "HDG", "NAV", "ALT" };

	/**
	 * Creates a new ACARS Route Entry bean.
	 * @param dt the date/time of this entry
	 * @param lat the aircraft's latitude
	 * @param lon the aircraft's longitude
	 * @see RouteEntry#getDate()
	 * @see RouteEntry#getPosition()
	 */
	public RouteEntry(Date dt, double lat, double lon) {
		super();
		_date = dt;
		_gpos = new GeoPosition(lat, lon);
	}

	/**
	 * Returns the aircraft's atltiude.
	 * @return the altitude in feet MSL
	 * @see RouteEntry#setAltitude(int)
	 */
	public int getAltitude() {
		return _alt;
	}

	/**
	 * Returns the aircraft's heading.
	 * @return the heading in degrees
	 * @see RouteEntry#setHeading(int)
	 */
	public int getHeading() {
		return _hdg;
	}

	/**
	 * Returns the date/time of this entry.
	 * @return the date/time of this entry
	 * @see RouteEntry#RouteEntry(Date, double, double)
	 */
	public Date getDate() {
		return _date;
	}

	/**
	 * Returns the flap detent position.
	 * @return the flap detent
	 * @see RouteEntry#setFlaps(int)
	 */
	public int getFlaps() {
		return _flaps;
	}

	/**
	 * Returns the ACARS flags for this entry.
	 * @return the flags
	 * @see RouteEntry#isFlagSet(int)
	 * @see RouteEntry#setFlag(int, boolean)
	 * @see RouteEntry#setFlags(int)
	 */
	public int getFlags() {
		return _flags;
	}

	/**
	 * Returns the aircraft's position.
	 * @return the position
	 * @see RouteEntry#RouteEntry(Date, double, double)
	 */
	public GeoPosition getPosition() {
		return _gpos;
	}
	
	/**
	 * Returns the aircraft's Mach number. 
	 * @return the Mach number
	 * @see RouteEntry#setMach(double)
	 */
	public double getMach() {
	   return _mach;
	}

	public double getLatitude() {
		return _gpos.getLatitude();
	}

	public double getLongitude() {
		return _gpos.getLongitude();
	}

	/**
	 * Returns the aircraft's airspeed.
	 * @return the airspeed in knots
	 * @see RouteEntry#setAirSpeed(int)
	 */
	public int getAirSpeed() {
		return _aSpeed;
	}

	/**
	 * Returns the aircraft's ground speed.
	 * @return the ground speed in knots
	 * @see RouteEntry#setGroundSpeed(int)
	 */
	public int getGroundSpeed() {
		return _gSpeed;
	}

	/**
	 * Returns the aircraft's vertical speed.
	 * @return the vertical speed in feet/minute
	 * @see RouteEntry#setVerticalSpeed(int)
	 */
	public int getVerticalSpeed() {
		return _vSpeed;
	}

	/**
	 * Returns the average N1 speed of all engines.
	 * @return the average N1 percentage
	 * @see RouteEntry#setN1(double)
	 */
	public double getN1() {
		return _n1;
	}

	/**
	 * Returns the average N2 speed of all engines.
	 * @return the average N2 percentage
	 * @see RouteEntry#setN2(double)
	 */
	public double getN2() {
		return _n2;
	}

	/**
	 * Returns if an ACARS flag was set.
	 * @param attrMask the flag attribute mask
	 * @return TRUE if the flag(s) were set, otherwise FALSE
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#setFlag(int, boolean)
	 * @see RouteEntry#setFlags(int)
	 */
	public boolean isFlagSet(int attrMask) {
		return ((_flags & attrMask) != 0);
	}

	/**
	 * Updates the aircraft's altitude.
	 * @param alt the altitude in feet
	 * @throws IllegalArgumentException if alt < -300 or alt > 100000
	 * @see RouteEntry#getAltitude()
	 */
	public void setAltitude(int alt) {
		if ((alt < -300) || (alt > 100000))
			throw new IllegalArgumentException("Altitude cannot be < -300 or > 100000");

		_alt = alt;
	}

	/**
	 * Updates the aircraft's heading.
	 * @param hdg the heading in degrees
	 * @throws IllegalArgumentException if hdg < 0 or hdg > 360
	 * @see RouteEntry#getHeading()
	 */
	public void setHeading(int hdg) {
		if ((hdg < 0) || (hdg > 360))
			throw new IllegalArgumentException("Heading cannot be < 0 or > 360 degrees");

		_hdg = hdg;
	}

	/**
	 * Updates the aircraft's airspeed.
	 * @param speed the speed in knots
	 * @throws IllegalArgumentException if speed < -20 or speed > 700
	 * @see RouteEntry#getAirSpeed()
	 */
	public void setAirSpeed(int speed) {
		if ((speed < -20) || (speed > 700))
			throw new IllegalArgumentException("Airspeed cannot be < -20 or > 700");

		_aSpeed = speed;
	}

	/**
	 * Updates the aircraft's ground speed.
	 * @param speed the speed in knots
	 * @throws IllegalArgumentException if speed < -5 or speed > 1500
	 * @see RouteEntry#getGroundSpeed()
	 */
	public void setGroundSpeed(int speed) {
		if ((speed < -5) || (speed > 1500))
			throw new IllegalArgumentException("Ground speed cannot be < -5 or > 1500");

		_gSpeed = speed;
	}

	/**
	 * Updates the aircraft's vertical speed.
	 * @param speed the speed in feet per minute
	 * @throws IllegalArgumentException if speed < -7000 or speed > 7000
	 * @see RouteEntry#getGroundSpeed()
	 */
	public void setVerticalSpeed(int speed) {
		if ((speed < -7000) || (speed > 7000))
			throw new IllegalArgumentException("Vertical speed cannot be < -7000 or > 7000");

		_vSpeed = speed;
	}
	
	/**
	 * Updates the aircraft's Mach number.
	 * @param mach the Mach number
	 * @throws IllegalArgumentException if mach < 0 or mach > 5.0
	 * @see RouteEntry#getMach()
	 */
	public void setMach(double mach) {
	   if ((mach < 0) || (mach > 5.0))
	      throw new IllegalArgumentException("Invalid Mach Number - " + mach);
	   
	   _mach = mach;
	}

	/**
	 * Updates the aircraft's average N1 speed.
	 * @param nn1 the N1 speed as a percentage
	 * @throws IllegalArgumentException if nn1 is negative or > 115
	 * @see RouteEntry#getN1()
	 */
	public void setN1(double nn1) {
		if ((nn1 < 0) || (nn1 > 115.0))
			throw new IllegalArgumentException("N1 cannot be negative or > 115.0%");

		_n1 = nn1;
	}

	/**
	 * Updates the aircraft's average N2 speed.
	 * @param nn2 the N2 speed as a percentage
	 * @throws IllegalArgumentException if nn2 is negative or > 115
	 * @see RouteEntry#getN2()
	 */
	public void setN2(double nn2) {
		if ((nn2 < 0) || (nn2 > 115.0))
			throw new IllegalArgumentException("N2 cannot be negative or > 115.0%");

		_n2 = nn2;
	}

	/**
	 * Updates the aircraft's flap detent position.
	 * @param flapDetent the detent position
	 * @throws IllegalArgumentException if flapDetent is negative or > 100
	 * @see RouteEntry#getFlaps()
	 */
	public void setFlaps(int flapDetent) {
		if ((flapDetent < 0) || (flapDetent > 100))
			throw new IllegalArgumentException("Invalid flap detent - " + flapDetent);

		_flaps = flapDetent;
	}

	/**
	 * Sets or clears an ACARS flag.
	 * @param attrMask the attribute mask
	 * @param isSet TRUE if the flag is set, FALSE if it is cleared
	 * @see RouteEntry#setFlags(int)
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#isFlagSet(int)
	 */
	public void setFlag(int attrMask, boolean isSet) {
		_flags = (isSet) ? (_flags | attrMask) : (_flags & (~attrMask));
	}

	/**
	 * Sets all ACARS flags for this entry.
	 * @param flags the flags
	 * @see RouteEntry#setFlag(int, boolean)
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#isFlagSet(int)
	 */
	public void setFlags(int flags) {
		_flags = flags;
	}

	/**
	 * Compares two route entries by comparing their date/times.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		RouteEntry re2 = (RouteEntry) o2;
		return _date.compareTo(re2.getDate());
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return MapEntry#White
	 */
	public String getIconColor() {
		return YELLOW;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuffer buf = new StringBuffer("<span class=\"small\">Position: <b>");
		buf.append(StringUtils.format(_gpos, true, GeoLocation.ALL));
		buf.append("</b><br /> Altitude: ");
		buf.append(StringUtils.format(_alt, "#,000"));
		buf.append(" feet<br />Speed: ");
		buf.append(StringUtils.format(_aSpeed, "##0"));
		buf.append(" kts (GS: ");
		buf.append(StringUtils.format(_gSpeed, "#,##0"));
		buf.append(" kts)<br />Heading: ");
		buf.append(StringUtils.format(_hdg, "000"));
		buf.append(" degrees<br />Veritical Speed: ");
		buf.append(StringUtils.format(_vSpeed, "###0"));
		buf.append(" feet/min<br />N<sub>1</sub>: ");
		buf.append(StringUtils.format(_n1, "##0.0"));
		buf.append("%, N<sub>2</sub>: ");
		buf.append(StringUtils.format(_n2, "##0.0"));
		buf.append("%<br />");

		// Add flaps logging if deployed
		if (_flaps > 0) {
			buf.append("Flaps: ");
			buf.append(String.valueOf(_flaps));
			buf.append("<sup>o</sup><br />");
		}

		// Add Autopilot flags if set
		if (isFlagSet(ACARSFlags.FLAG_AUTOPILOT)) {
			buf.append("Autopilot: ");
			for (int x = 0; x < AP_FLAGS.length; x++) {
				if (isFlagSet(AP_FLAGS[x])) {
					buf.append(AP_FLAG_NAMES[x]);
					buf.append(' ');
				}
			}

			buf.append("<br />");
		}

		// Add Autothrottle flags if set
		if (isFlagSet(ACARSFlags.FLAG_AT_IAS)) {
			buf.append("Autothrottle: IAS");
		} else if (isFlagSet(ACARSFlags.FLAG_AT_MACH)) {
			buf.append("Autothrottle: MACH");
		}

		buf.append("<br /></span>");

		return buf.toString();
	}
}