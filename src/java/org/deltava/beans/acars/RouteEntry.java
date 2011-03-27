// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.servinfo.Controller;

import org.deltava.util.StringUtils;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

public class RouteEntry extends ACARSMapEntry implements GeospaceLocation {

	private Date _date;
	private FlightPhase _phase;

	private int _alt;
	private int _radarAlt;
	private int _hdg;
	private double _pitch;
	private double _bank;
	private int _aSpeed;
	private int _gSpeed;
	private int _vSpeed;
	private double _aoa;
	private double _gForce;
	private double _n1;
	private double _n2;
	private double _mach;
	private int _wSpeed;
	private int _wHdg;
	private double _viz;
	private int _fuelFlow;
	private int _flaps;
	private int _flags;
	private int _frameRate;
	private int _simRate;
	private int _fuelRemaining;
	
	private String _com1;
	private Controller _atc;

	private static final int[] AP_FLAGS = { FLAG_AP_APR, FLAG_AP_HDG, FLAG_AP_NAV, FLAG_AP_ALT, FLAG_AP_GPS , FLAG_AP_LNAV};
	private static final String[] AP_FLAG_NAMES = { "APR", "HDG", "NAV", "ALT", "GPS", "LNAV" };
	
	private static final String[] PHASE_NAMES = {"Unknown", "Pre-Flight", "Pushback", "Taxi Out", "Takeoff", "Airborne", "Rollout",
			"Taxi In", "At Gate", "Shutdown", "Completed", "Abort", "Error", "File PIREP" };

	/**
	 * Creates a new ACARS Route Entry bean.
	 * @param dt the date/time of this entry
	 * @param loc the aircraft's location
	 * @see RouteEntry#getDate()
	 * @see RouteEntry#getLocation()
	 */
	public RouteEntry(Date dt, GeoLocation loc) {
		super(loc);
		_date = dt;
	}
	
	/**
	 * Returns if this entry is a Dispatcher.
	 */
	public final boolean isDispatch() {
		return false;
	}

	/**
	 * Returns the aircraft's atltiude above <i>sea level</i>.
	 * @return the altitude in feet MSL
	 * @see RouteEntry#setAltitude(int)
	 * @see RouteEntry#getRadarAltitude()
	 */
	public int getAltitude() {
		return _alt;
	}

	/**
	 * Returns the aircraft altitude above <i>ground level</i>.
	 * @return the altitude in feet AGL
	 * @see RouteEntry#setRadarAltitude(int)
	 */
	public int getRadarAltitude() {
		return _radarAlt;
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
	 * Returns the aircraft's Angle of Attack.
	 * @return the angle of attack in degrees
	 * @see RouteEntry#setAOA(double)
	 */
	public double getAOA() {
		return _aoa;
	}

	/**
	 * Returns the G forces acting on the aircraft.
	 * @return the force in Gs
	 * @see RouteEntry#setG(double)
	 */
	public double getG() {
		return _gForce;
	}

	/**
	 * Returns the aircraft's pitch angle.
	 * @return the pitch in degrees
	 * @see RouteEntry#setPitch(double)
	 */
	public double getPitch() {
		return _pitch;
	}

	/**
	 * Returns the aircraft's bank angle.
	 * @return the bank in degrees
	 * @see RouteEntry#setBank(double)
	 */
	public double getBank() {
		return _bank;
	}

	/**
	 * Returns the aircraft's fuel flow for all engines.
	 * @return the flow in pounds per hour
	 * @see RouteEntry#setFuelFlow(int)
	 */
	public int getFuelFlow() {
		return _fuelFlow;
	}

	/**
	 * Returns the Flight Simulator frame rate.
	 * @return the number of rendered frames per second
	 * @see RouteEntry#setFrameRate(int)
	 */
	public int getFrameRate() {
		return _frameRate;
	}

	/**
	 * Returns the date/time of this entry.
	 * @return the date/time of this entry
	 * @see RouteEntry#RouteEntry(Date, GeoLocation)
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
	 * Returns the visibility.
	 * @return the visibility in miles
	 */
	public double getVisibility() {
		return _viz;
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
	 * Returns the Flight Simulator time acceleration rate.
	 * @return the acceleration rate
	 * @see RouteEntry#setSimRate(int)
	 */
	public int getSimRate() {
		return _simRate;
	}
	
	/**
	 * Returns the amount of fuel remaining on the aircraft.
	 * @return the fuel in pounds
	 * @see RouteEntry#setFuelRemaining(int)
	 */
	public int getFuelRemaining() {
		return _fuelRemaining;
	}
	
	/**
	 * Returns the flight phase.
	 * @return the phase code
	 * @see RouteEntry#getPhaseName()
	 * @see RouteEntry#setPhase(int)
	 * @see RouteEntry#setPhase(String)
	 */
	public FlightPhase getPhase() {
		return _phase;
	}
	
	/**
	 * Returns the flight phase name for display in a JSP or HTML.
	 * @return the phase name
	 * @see RouteEntry#getPhase()
	 * @see RouteEntry#setPhase(int)
	 * @see RouteEntry#setPhase(String)
	 */
	public String getPhaseName() {
		return PHASE_NAMES[_phase.getPhase()];
	}
	
	/**
	 * Returns the COM1 frequency.
	 * @return the frequency
	 * @see RouteEntry#setCOM1(String)
	 */
	public String getCOM1() {
		return _com1;
	}
	
	/**
	 * Returns the Controller on COM1. 
	 * @return a Controller bean, or null if none
	 * @see RouteEntry#setController(Controller)
	 */
	public Controller getController() {
		return _atc;
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
	 * Updates the aircraft's altitude above <i>sea level</i>.
	 * @param alt the altitude in feet MSL
	 * @throws IllegalArgumentException if alt < -300 or alt > 120000
	 * @see RouteEntry#getAltitude()
	 * @see RouteEntry#setRadarAltitude(int)
	 */
	public void setAltitude(int alt) {
		if ((alt < -300) || (alt > 120000))
			throw new IllegalArgumentException("Altitude cannot be < -300 or > 120000 - " + alt);

		_alt = alt;
	}

	/**
	 * Updates the aircraft's altitude above <i>ground level</i>.
	 * @param alt the altitude in feet AGL
	 * @see RouteEntry#getRadarAltitude()
	 * @see RouteEntry#setAltitude(int)
	 */
	public void setRadarAltitude(int alt) {
		_radarAlt = (alt < 0) ? 0 : alt;
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
	 * Updates the aircraft's Angle of Attack.
	 * @param aoa the angle of attack in degrees
	 * @see RouteEntry#setAOA(double)
	 */
	public void setAOA(double aoa) {
		_aoa = Math.max(-99.9, Math.min(99.9, aoa));
	}

	/**
	 * Updates the G forces acting on the aircraft.
	 * @param gForce the force in Gs
	 * @see RouteEntry#getG()
	 */
	public void setG(double gForce) {
		_gForce = gForce;
	}

	/**
	 * Updates the aircraft's pitch angle.
	 * @param p the pitch in degrees
	 * @see RouteEntry#getPitch()
	 */
	public void setPitch(double p) {
		_pitch = Math.max(-90, Math.min(90, p));
	}

	/**
	 * Updates the aircraft's bank angle.
	 * @param b the bank in degrees
	 * @throws IllegalArgumentException if b < -90 or > 90
	 * @see RouteEntry#getBank()
	 */
	public void setBank(double b) {
		if ((b < -170) || (b > 170))
			throw new IllegalArgumentException("Bank angle cannot be < -170 or > 170 degrees");

		_bank = b;
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
	 * Updates the aircraft's vertical speed.
	 * @param speed the speed in feet per minute
	 * @throws IllegalArgumentException if speed < -12000 or speed > 12000
	 * @see RouteEntry#getGroundSpeed()
	 */
	public void setVerticalSpeed(int speed) {
		if ((speed < -12000) || (speed > 12000))
			throw new IllegalArgumentException("Vertical speed cannot be < -12000 or > 12000 - " + speed);

		_vSpeed = speed;
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
	 * Updates the aircraft's average N1 speed.
	 * @param nn1 the N1 speed as a percentage
	 * @see RouteEntry#getN1()
	 */
	public void setN1(double nn1) {
		_n1 = Math.max(0, nn1);
	}

	/**
	 * Updates the aircraft's average N2 speed.
	 * @param nn2 the N2 speed as a percentage
	 * @see RouteEntry#getN2()
	 */
	public void setN2(double nn2) {
		_n2 = Math.max(0, nn2);
	}

	/**
	 * Updates the aircraft's total fuel flow.
	 * @param flow the flow in pounds per hour
	 * @see RouteEntry#getFuelFlow()
	 */
	public void setFuelFlow(int flow) {
		_fuelFlow = Math.max(0, flow);
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
	 * Updates the Flight Simulator frame rate.
	 * @param rate the rendered frames per second
	 * @see RouteEntry#getFrameRate()
	 */
	public void setFrameRate(int rate) {
		_frameRate = rate;
	}
	
	/**
	 * Updates the Flight Simulator time acceleration rate.
	 * @param rate the rate
	 * @see RouteEntry#getSimRate()
	 */
	public void setSimRate(int rate) {
		_simRate = (rate < 0) ? 1 : rate;
	}
	
	/**
	 * Updates the flight phase.
	 * @param phase the phase code
	 * @throws IllegalArgumentException if phase is invalid
	 * @see RouteEntry#setPhase(String)
	 * @see RouteEntry#getPhase()
	 */
	public void setPhase(int phase) {
		if ((phase < 0) || (phase >= PHASE_NAMES.length))
			throw new IllegalArgumentException("Invalid Flight phase - " + phase);
		
		_phase = FlightPhase.values()[phase];
	}

	/**
	 * Updates the flight phase.
	 * @param phaseName the phase name
	 * @throws IllegalArgumentException if phase is invalid
	 * @see RouteEntry#setPhase(int)
	 * @see RouteEntry#getPhase()
	 */
	public void setPhase(String phaseName) {
		setPhase(StringUtils.arrayIndexOf(PHASE_NAMES, phaseName));
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
	 * Sets the visibility.
	 * @param viz the visibility in miles
	 */
	public void setVisibility(double viz) {
		_viz = Math.max(0, viz);
	}
	
	/**
	 * Sets the COM1 radio frequency.
	 * @param com1 the frequency
	 * @see RouteEntry#getCOM1()
	 */
	public void setCOM1(String com1) {
		_com1 = com1;
	}
	
	/**
	 * Sets the controller on COM1.
	 * @param atc a Controller bean
	 * @see RouteEntry#getController()
	 */
	public void setController(Controller atc) {
		_atc = atc;
	}
	
	/**
	 * Marks this route entry as having a notable flight parameter.
	 * @return TRUE if the entry should be noted, otherwise FALSE
	 */
	public boolean isWarning() {
		return (getWarning() != null);
	}
	
	/**
	 * Returns the warning message.
	 * @return the warning
	 */
	public String getWarning() {
		Collection<String> warnings = new ArrayList<String>();
		if ((_alt < 10000) && (_aSpeed > 250))
			warnings.add("250 UNDER 10K");
		if ((_radarAlt < 1500) && (_vSpeed < -1500))
			warnings.add("DESCENT RATE");
		if (Math.abs(_bank) > 45)
			warnings.add("BANK");
		if (Math.abs(_pitch) > 35)
			warnings.add("PITCH");
		if (Math.abs(1 - _gForce) >= 0.25)
			warnings.add("G-FORCE");
		if (_fuelRemaining == 0)
			warnings.add("NO FUEL");
		if (isFlagSet(FLAG_STALL))
			warnings.add("STALL");
		if (isFlagSet(FLAG_OVERSPEED))
			warnings.add("OVERSPEED");
		if (isFlagSet(FLAG_GEARDOWN) && (_aSpeed > 250))
			warnings.add("GEAR SPEED");
		if (!isFlagSet(FLAG_GEARDOWN) && isFlagSet(FLAG_ONGROUND))
			warnings.add("GEAR UP");
		if (isFlagSet(FLAG_CRASH))
			warnings.add("CRASH");
		
		return warnings.isEmpty() ? null : StringUtils.listConcat(warnings, " ");
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
	 * Return the Google Maps icon color.
	 */
	public String getIconColor() {
		if (isFlagSet(FLAG_TOUCHDOWN))
			return PURPLE;
		else if (isWarning())
			return RED;
		else if (isFlagSet(FLAG_AP_ANY))
			return WHITE;

		return GREY;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder(192);
		buf.append("<span class=\"mapInfoBox\">Position: <span class=\"bld\">");
		buf.append(StringUtils.format(_pos, true, GeoLocation.ALL));
		buf.append("</span><br />Altitude: ");
		buf.append(StringUtils.format(_alt, "#,000"));
		buf.append(" feet");
		if ((_radarAlt > 0) && (_radarAlt < 2500)) {
			buf.append(" (");
			buf.append(StringUtils.format(_radarAlt, "#,000"));
			buf.append(" feet AGL)");
		}

		buf.append("<br />");
		if ((_pitch < -1) || (_pitch > 5)) {
			buf.append("Pitch: ");
			buf.append(StringUtils.format(_pitch, "#0.0"));
			buf.append("<sup>o</sup>");
			if (Math.abs(_bank) > 3)
				buf.append(' ');
			else
				buf.append("<br />");
		}

		if (Math.abs(_bank) > 3) {
			buf.append("Bank: ");
			buf.append(StringUtils.format(_bank, "#0.0"));
			buf.append("<sup>o</sup><br />");
		}

		if (Math.abs(1 - _gForce) >= 0.1) {
			buf.append("Acceleration: ");
			buf.append(StringUtils.format(_gForce, "#0.000"));
			buf.append("G<br />");
		}

		buf.append("Speed: ");
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
		buf.append(" degrees<br />Vertical Speed: ");
		buf.append(StringUtils.format(_vSpeed, "###0"));
		buf.append(" feet/min<br />");
		if (_n1 > 155) {
			buf.append(StringUtils.format(_n1, "###0"));
			buf.append(" RPM, ");
			buf.append(StringUtils.format(_n2, "##0.0"));
			buf.append("% throttle");
		} else {
			buf.append("N<sub>1</sub>: ");	
			buf.append(StringUtils.format(_n1, "##0.0"));
			buf.append("%, N<sub>2</sub>: ");
			buf.append(StringUtils.format(_n2, "##0.0"));
			buf.append('%');
		}
		
		buf.append("<br />Fuel Flow:");
		buf.append(StringUtils.format(_fuelFlow, "#,##0"));
		buf.append(" lbs/hr<br />");

		// Add flaps logging if deployed
		if (_flaps > 0) {
			buf.append("Flaps: ");
			buf.append(String.valueOf(_flaps));
			buf.append("<sup>o</sup><br />");
		}

		// Add afterburner/gear if deployed
		if (isFlagSet(FLAG_AFTERBURNER))
			buf.append("<span class=\"bld ita\">AFTERBURNER</span><br />");
		if (isFlagSet(FLAG_GEARDOWN) && !isFlagSet(FLAG_ONGROUND))
			buf.append("<span class=\"ita\">GEAR DOWN</span><br />");

		// Add Autopilot flags if set
		if (isFlagSet(FLAG_AP_ANY)) {
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
		if (isFlagSet(FLAG_AT_VNAV))
			buf.append("Autothrottle: VNAV<br />");
		else if (isFlagSet(FLAG_AT_IAS))
			buf.append("Autothrottle: IAS<br />");
		else if (isFlagSet(FLAG_AT_MACH))
			buf.append("Autothrottle: MACH<br />");
		
		// Add Pause/Stall/Warning flags
		if (isFlagSet(FLAG_PAUSED))
			buf.append("<span class=\"error\">FLIGHT PAUSED</span><br />");
		String warn = getWarning();
		if (warn != null) {
			buf.append("<span class=\"error bld\">");
			buf.append(warn);
			buf.append("</span>");
		}
		
		// Add ATC info
		if (_atc != null) {
			buf.append("<br />COM1: ");
			buf.append(_com1);
			buf.append(" (");
			buf.append(_atc.getCallsign());
			buf.append(')');
		}

		buf.append("</span>");
		return buf.toString();
	}
}