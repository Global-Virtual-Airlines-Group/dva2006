// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.flight.Warning;
import org.deltava.beans.navdata.AirspaceType;

import org.deltava.util.StringUtils;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 8.5
 * @since 1.0
 */

public abstract class RouteEntry extends ACARSMapEntry implements GeospaceLocation, CalendarEntry, FuelChecker {

	private final Instant _date;
	private FlightPhase _phase;
	private AirspaceType _asType = AirspaceType.E;

	private int _alt;
	private int _hdg;
	private int _aSpeed;
	private int _gSpeed;
	private double _mach;
	private int _wSpeed;
	private int _wHdg;
	private int _fuelRemaining;
	
	private int _flags;
	
	/**
	 * Creates a new ACARS Route Entry bean.
	 * @param dt the date/time of this entry
	 * @param loc the aircraft's location
	 * @see RouteEntry#getDate()
	 * @see RouteEntry#getLocation()
	 */
	protected RouteEntry(GeoLocation loc, Instant dt) {
		super(loc);
		_date = dt;
	}
	
	/**
	 * Returns the aircraft's atltiude above <i>sea level</i>.
	 * @return the altitude in feet MSL
	 * @see RouteEntry#setAltitude(int)
	 */
	@Override
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
	 * Returns the aircraft's altitude above <i>ground level<i>, if implemented. Subclasses that
	 * do not implement this may return a negative number.
	 * @return the altitude in feet AGL, or a negative number
	 */
	public abstract int getRadarAltitude();
	
	/**
	 * Returns the ACARS flags for this entry.
	 * @return the flags
	 * @see RouteEntry#isFlagSet(ACARSFlags)
	 * @see RouteEntry#setFlag(ACARSFlags, boolean)
	 * @see RouteEntry#setFlags(int)
	 */
	public int getFlags() {
		return _flags;
	}

	/**
	 * Returns the date/time of this entry.
	 * @return the date/time of this entry
	 * @see RouteEntry#RouteEntry(GeoLocation, Instant)
	 */
	@Override
	public Instant getDate() {
		return _date;
	}

	/**
	 * Returns the aircraft's Mach number.
	 * @return the Mach number
	 * @see RouteEntry#setMach(double)
	 */
	public double getMach() {
		return _mach;
	}

	/**
	 * Returns the ambient wind speed.
	 * @return the speed in knots
	 */
	public int getWindSpeed() {
		return _wSpeed;
	}

	/**
	 * Returns the ambient wind heading.
	 * @return the wind heading in degrees
	 */
	public int getWindHeading() {
		return _wHdg;
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
	 * Returns the amount of fuel remaining on the aircraft.
	 * @return the fuel in pounds
	 * @see RouteEntry#setFuelRemaining(int)
	 */
	@Override
	public int getFuelRemaining() {
		return _fuelRemaining;
	}
	
	/**
	 * Returns the flight phase.
	 * @return the phase code
	 * @see RouteEntry#setPhase(FlightPhase)
	 */
	public FlightPhase getPhase() {
		return _phase;
	}
	
	/**
	 * Returns the type of Airspace covering the current position.
	 * @return an AirspaceType enumeration
	 * @see RouteEntry#setAirspace(AirspaceType)
	 */
	public AirspaceType getAirspace() {
		return _asType;
	}
	
	@Override
	public final EntryType getType() {
		return EntryType.AIRCRAFT;
	}

	/**
	 * Returns if an ACARS flag was set.
	 * @param flag the ACARSFlag
	 * @return TRUE if the flag(s) were set, otherwise FALSE
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#setFlag(ACARSFlags, boolean)
	 * @see RouteEntry#setFlags(int)
	 */
	@Override
	public boolean isFlagSet(ACARSFlags flag) {
		return flag.has(_flags);
	}

	/**
	 * Updates the aircraft's altitude above <i>sea level</i>.
	 * @param alt the altitude in feet MSL
	 * @throws IllegalArgumentException if alt < -300 or alt > 120000
	 * @see RouteEntry#getAltitude()
	 */
	public void setAltitude(int alt) {
		if ((alt < -300) || (alt > 120000))
			throw new IllegalArgumentException("Altitude cannot be < -300 or > 120000 - " + alt);

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
			throw new IllegalArgumentException("Airspeed cannot be < -20 or > 700 - " + speed);

		_aSpeed = speed;
	}

	/**
	 * Updates the aircraft's ground speed.
	 * @param speed the speed in knots
	 * @throws IllegalArgumentException if speed < -5 or speed > 1600
	 * @see RouteEntry#getGroundSpeed()
	 */
	public void setGroundSpeed(int speed) {
		_gSpeed = speed;
	}

	/**
	 * Updates the aircraft's Mach number.
	 * @param mach the Mach number
	 * @throws IllegalArgumentException if mach > 5.0
	 * @see RouteEntry#getMach()
	 */
	public void setMach(double mach) {
		if (mach > 5.0)
			throw new IllegalArgumentException("Invalid Mach Number - " + mach);

		_mach = Math.max(0, mach);
	}

	/**
	 * Updates the amount of fuel remaining on the aircraft.
	 * @param fuel the amount of fuel in pounds
	 * @see RouteEntry#getFuelRemaining()
	 */
	public void setFuelRemaining(int fuel) {
		_fuelRemaining = Math.max(0, fuel);
	}

	/**
	 * Updates the flight phase.
	 * @param phase the FlightPhase
	 * @see RouteEntry#getPhase()
	 */
	public void setPhase(FlightPhase phase) {
		_phase = phase;
	}

	/**
	 * Sets or clears an ACARS flag.
	 * @param flag the ACARS
	 * @param isSet TRUE if the flag is set, FALSE if it is cleared
	 * @see RouteEntry#setFlags(int)
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#isFlagSet(ACARSFlags)
	 */
	public void setFlag(ACARSFlags flag, boolean isSet) {
		_flags = (isSet) ? (_flags | flag.getMask()) : (_flags & (~flag.getMask()));
	}

	/**
	 * Sets all ACARS flags for this entry.
	 * @param flags the flags
	 * @see RouteEntry#setFlag(ACARSFlags, boolean)
	 * @see RouteEntry#getFlags()
	 * @see RouteEntry#isFlagSet(ACARSFlags)
	 */
	public void setFlags(int flags) {
		_flags = flags;
	}

	/**
	 * Sets the ambient wind speed.
	 * @param speed the speed in knots
	 * @see RouteEntry#getWindSpeed()
	 * @see RouteEntry#setWindSpeed(int)
	 */
	public void setWindSpeed(int speed) {
		_wSpeed = Math.max(0, speed);
	}

	/**
	 * Sets the ambient wind heading.
	 * @param hdg the heading in degrees
	 * @see RouteEntry#getWindHeading()
	 * @see RouteEntry#setWindSpeed(int)
	 */
	public void setWindHeading(int hdg) {
		if ((hdg >= 0) && (hdg < 360))
			_wHdg = hdg;
	}
	
	/**
	 * Sets the Airspace type covering the current position.
	 * @param at the AirspaceType
	 * @see RouteEntry#getAirspace()
	 */
	public void setAirspace(AirspaceType at) {
		_asType = at;
	}

	/**
	 * Marks this route entry as having a notable flight parameter.
	 * @return TRUE if the entry should be noted, otherwise FALSE
	 */
	public boolean isWarning() {
		return (getWarnings().size() > 0);
	}
	
	/**
	 * Returns the warning message.
	 * @return the warning
	 */
	public abstract Collection<Warning> getWarnings();

	/**
	 * Compares two route entries by comparing their date/times.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Object o2) {
		RouteEntry re2 = (RouteEntry) o2;
		return _date.compareTo(re2.getDate());
	}

	@Override
	public String getIconColor() {
		if (ACARSFlags.TOUCHDOWN.has(_flags))
			return PURPLE;
		else if (isWarning())
			return RED;
		else if (ACARSFlags.hasAP(_flags))
			return WHITE;

		return GREY;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder(192);
		buf.append("<div class=\"mapInfoBox acarsPosition\">Position: <span class=\"bld\">");
		buf.append(StringUtils.format(_pos, true, GeoLocation.ALL));
		buf.append("</span><br />Time: ");
		buf.append(StringUtils.format(_date, "MM/dd/yyyy HH:mm:ss"));
		buf.append(" UTC<br />Altitude: ");
		buf.append(StringUtils.format(_alt, "#,000"));
		buf.append(" feet<br />Speed: ");
		buf.append(StringUtils.format(_aSpeed, "##0"));
		buf.append(" kts (GS: ");
		buf.append(StringUtils.format(_gSpeed, "#,##0"));
		buf.append(" kts)");
		if (_mach > 0.6) {
			buf.append(" <i>Mach ");
			buf.append(StringUtils.format(_mach, "0.00"));
			buf.append("</i>");
		}

		buf.append("<br />Heading: ");
		buf.append(StringUtils.format(_hdg, "000"));
		buf.append(" degrees<br />");

		// Add Pause/Stall/Warning flags
		if (ACARSFlags.PAUSED.has(_flags))
			buf.append("<span class=\"error\">FLIGHT PAUSED</span><br />");
		Collection<Warning> warns = getWarnings();
		if (!warns.isEmpty()) {
			buf.append("<span class=\"error bld caps\">");
			for (Iterator<Warning> i = warns.iterator(); i.hasNext(); ) {
				buf.append(i.next().getDescription());
				if (i.hasNext())
					buf.append(' ');
			}
					
			buf.append("</span>");
		}
		
		buf.append("</div>");
		return buf.toString();
	}
}