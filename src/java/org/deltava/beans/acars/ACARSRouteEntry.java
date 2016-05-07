// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.servinfo.Controller;

import org.deltava.util.StringUtils;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @author Rahul
 * @version 7.0
 * @since 1.0
 */

public class ACARSRouteEntry extends RouteEntry {

	private int _radarAlt;
	private double _pitch;
	private double _bank;
	private int _vSpeed;
	private double _aoa;
	private double _gForce;
	private double _n1;
	private double _n2;
	
	private double _viz;
	private int _temp;
	private int _pressure;
	
	private int _fuelFlow;
	private int _flaps;
	private int _frameRate;
	private int _simRate;
	
	private Instant _simTime;
	
	private String _nav1;
	private String _nav2;
	
	private String _com1;
	private String _com2;	
	private Controller _atc1;
	private Controller _atc2;

	private static final int[] AP_FLAGS = { FLAG_AP_APR, FLAG_AP_HDG, FLAG_AP_NAV, FLAG_AP_ALT, FLAG_AP_GPS , FLAG_AP_LNAV};
	private static final String[] AP_FLAG_NAMES = { "APR", "HDG", "NAV", "ALT", "GPS", "LNAV" };
	
	/**
	 * Creates a new ACARS Route Entry bean.
	 * @param dt the date/time of this entry
	 * @param loc the aircraft's location
	 * @see ACARSRouteEntry#getDate()
	 * @see ACARSRouteEntry#getLocation()
	 */
	public ACARSRouteEntry(Instant dt, GeoLocation loc) {
		super(loc, dt);
	}
	
	/**
	 * Returns the aircraft altitude above <i>ground level</i>.
	 * @return the altitude in feet AGL
	 * @see ACARSRouteEntry#setRadarAltitude(int)
	 */
	@Override
	public int getRadarAltitude() {
		return _radarAlt;
	}

	/**
	 * Returns the aircraft's Angle of Attack.
	 * @return the angle of attack in degrees
	 * @see ACARSRouteEntry#setAOA(double)
	 */
	public double getAOA() {
		return _aoa;
	}

	/**
	 * Returns the G forces acting on the aircraft.
	 * @return the force in Gs
	 * @see ACARSRouteEntry#setG(double)
	 */
	public double getG() {
		return _gForce;
	}

	/**
	 * Returns the aircraft's pitch angle.
	 * @return the pitch in degrees
	 * @see ACARSRouteEntry#setPitch(double)
	 */
	public double getPitch() {
		return _pitch;
	}

	/**
	 * Returns the aircraft's bank angle.
	 * @return the bank in degrees
	 * @see ACARSRouteEntry#setBank(double)
	 */
	public double getBank() {
		return _bank;
	}

	/**
	 * Returns the aircraft's fuel flow for all engines.
	 * @return the flow in pounds per hour
	 * @see ACARSRouteEntry#setFuelFlow(int)
	 */
	public int getFuelFlow() {
		return _fuelFlow;
	}

	/**
	 * Returns the Flight Simulator frame rate.
	 * @return the number of rendered frames per second
	 * @see ACARSRouteEntry#setFrameRate(int)
	 */
	public int getFrameRate() {
		return _frameRate;
	}

	/**
	 * Returns the flap detent position.
	 * @return the flap detent
	 * @see ACARSRouteEntry#setFlaps(int)
	 */
	public int getFlaps() {
		return _flaps;
	}

	/**
	 * Returns the visibility.
	 * @return the visibility in miles
	 */
	public double getVisibility() {
		return _viz;
	}
	
	/**
	 * Returns the ambient air temperature.
	 * @return the temperature in degrees Celsius
	 */
	public int getTemperature() {
		return _temp;
	}
	
	/**
	 * Returns the ambient air pressure.
	 * @return the pressure in pascals
	 */
	public int getPressure() {
		return _pressure;
	}

	/**
	 * Returns the aircraft's vertical speed.
	 * @return the vertical speed in feet/minute
	 * @see ACARSRouteEntry#setVerticalSpeed(int)
	 */
	public int getVerticalSpeed() {
		return _vSpeed;
	}

	/**
	 * Returns the average N1 speed of all engines.
	 * @return the average N1 percentage
	 * @see ACARSRouteEntry#setN1(double)
	 */
	public double getN1() {
		return _n1;
	}

	/**
	 * Returns the average N2 speed of all engines.
	 * @return the average N2 percentage
	 * @see ACARSRouteEntry#setN2(double)
	 */
	public double getN2() {
		return _n2;
	}
	
	/**
	 * Returns the simulator time acceleration rate.
	 * @return the acceleration rate
	 * @see ACARSRouteEntry#setSimRate(int)
	 */
	public int getSimRate() {
		return _simRate;
	}
	
	/**
	 * Returns the UTC time in the simulator.
	 * @return the sim UTC time
	 */
	public Instant getSimUTC() {
		return _simTime;
	}
	
	/**
	 * Returns the NAV1 frequency.
	 * @return the frequency
	 * @see ACARSRouteEntry#setNAV1(String)
	 */
	public String getNAV1() {
		return _nav1;
	}
	
	/**
	 * Returns the NAV2 frequency.
	 * @return the frequency
	 * @see ACARSRouteEntry#setNAV2(String)
	 */
	public String getNAV2() {
		return _nav2;
	}
	
	/**
	 * Returns the COM1 frequency.
	 * @return the frequency
	 * @see ACARSRouteEntry#setCOM1(String)
	 */
	public String getCOM1() {
		return _com1;
	}
	
	/**
	 * Returns the COM2 frequency.
	 * @return the frequency
	 * @see ACARSRouteEntry#setCOM2(String)
	 */
	public String getCOM2() {
		return _com2;
	}
	
	/**
	 * Returns the Controller on COM1. 
	 * @return a Controller bean, or null if none
	 * @see ACARSRouteEntry#setATC1(Controller)
	 */
	public Controller getATC1() {
		return _atc1;
	}
	
	/**
	 * Returns the Controller on COM2. 
	 * @return a Controller bean, or null if none
	 * @see ACARSRouteEntry#setATC2(Controller)
	 */
	public Controller getATC2() {
		return _atc2;
	}
	
	/**
	 * Updates the aircraft's altitude above <i>ground level</i>.
	 * @param alt the altitude in feet AGL
	 * @see ACARSRouteEntry#getRadarAltitude()
	 * @see ACARSRouteEntry#setAltitude(int)
	 */
	public void setRadarAltitude(int alt) {
		_radarAlt = Math.max(0, alt);
	}

	/**
	 * Updates the aircraft's Angle of Attack.
	 * @param aoa the angle of attack in degrees
	 * @see ACARSRouteEntry#setAOA(double)
	 */
	public void setAOA(double aoa) {
		_aoa = Math.max(-99.9, Math.min(99.9, aoa));
	}

	/**
	 * Updates the G forces acting on the aircraft.
	 * @param gForce the force in Gs
	 * @see ACARSRouteEntry#getG()
	 */
	public void setG(double gForce) {
		_gForce = gForce;
	}

	/**
	 * Updates the aircraft's pitch angle.
	 * @param p the pitch in degrees
	 * @see ACARSRouteEntry#getPitch()
	 */
	public void setPitch(double p) {
		_pitch = Math.max(-90, Math.min(90, p));
	}

	/**
	 * Updates the aircraft's bank angle.
	 * @param b the bank in degrees
	 * @throws IllegalArgumentException if b < -90 or > 90
	 * @see ACARSRouteEntry#getBank()
	 */
	public void setBank(double b) {
		if ((b < -170) || (b > 170))
			throw new IllegalArgumentException("Bank angle cannot be < -170 or > 170 degrees");

		_bank = b;
	}

	/**
	 * Updates the aircraft's vertical speed.
	 * @param speed the speed in feet per minute
	 * @throws IllegalArgumentException if speed < -12000 or speed > 12000
	 * @see ACARSRouteEntry#getGroundSpeed()
	 */
	public void setVerticalSpeed(int speed) {
		if ((speed < -12000) || (speed > 12000))
			throw new IllegalArgumentException("Vertical speed cannot be < -12000 or > 12000 - " + speed);

		_vSpeed = speed;
	}

	/**
	 * Updates the aircraft's average N1 speed.
	 * @param nn1 the N1 speed as a percentage
	 * @see ACARSRouteEntry#getN1()
	 */
	public void setN1(double nn1) {
		_n1 = Math.max(0, nn1);
	}

	/**
	 * Updates the aircraft's average N2 speed.
	 * @param nn2 the N2 speed as a percentage
	 * @see ACARSRouteEntry#getN2()
	 */
	public void setN2(double nn2) {
		_n2 = Math.max(0, nn2);
	}

	/**
	 * Updates the aircraft's total fuel flow.
	 * @param flow the flow in pounds per hour
	 * @see ACARSRouteEntry#getFuelFlow()
	 */
	public void setFuelFlow(int flow) {
		_fuelFlow = Math.max(0, flow);
	}
	
	/**
	 * Updates the Flight Simulator frame rate.
	 * @param rate the rendered frames per second
	 * @see ACARSRouteEntry#getFrameRate()
	 */
	public void setFrameRate(int rate) {
		_frameRate = rate;
	}
	
	/**
	 * Updates the simulator time acceleration rate.
	 * @param rate the rate
	 * @see ACARSRouteEntry#getSimRate()
	 */
	public void setSimRate(int rate) {
		_simRate = Math.max(1, rate);
	}
	
	/**
	 * Updates the UTC time in the simulator 
	 * @param i the UTC time
	 */
	public void setSimUTC(Instant i) {
		_simTime = i;
	}
	
	/**
	 * Updates the aircraft's flap detent position.
	 * @param flapDetent the detent position
	 * @throws IllegalArgumentException if flapDetent is negative or > 100
	 * @see ACARSRouteEntry#getFlaps()
	 */
	public void setFlaps(int flapDetent) {
		_flaps = Math.max(0, Math.min(100, flapDetent));
	}

	/**
	 * Sets the visibility.
	 * @param viz the visibility in miles
	 */
	public void setVisibility(double viz) {
		_viz = Math.max(0, viz);
	}
	
	/**
	 * Sets the ambient air pressure.
	 * @param p the pressure in pascals
	 */
	public void setPressure(int p) {
		_pressure = p;
	}
	
	/**
	 * Sets the ambient temperature.
	 * @param t the temperature in degrees celsius
	 */
	public void setTemperature(int t) {
		_temp = t;
	}
	
	/**
	 * Sets the COM1 radio frequency.
	 * @param freq the frequency
	 * @see ACARSRouteEntry#getCOM1()
	 */
	public void setCOM1(String freq) {
		_com1 = freq;
	}
	
	/**
	 * Sets the COM2 radio frequency.
	 * @param freq the frequency
	 * @see ACARSRouteEntry#getCOM2()
	 */
	public void setCOM2(String freq) {
		_com2 = freq;
	}
	
	/**
	 * Sets the NAV1 radio frequency.
	 * @param freq the frequency
	 * @see ACARSRouteEntry#getNAV1()
	 */
	public void setNAV1(String freq) {
		_nav1 = freq;
	}
	
	/**
	 * Sets the NAV2 radio frequency.
	 * @param freq the frequency
	 * @see ACARSRouteEntry#getNAV2()
	 */
	public void setNAV2(String freq) {
		_nav2 = freq;
	}
	
	/**
	 * Sets the controller on COM1.
	 * @param atc a Controller bean
	 * @see ACARSRouteEntry#getATC1()
	 */
	public void setATC1(Controller atc) {
		_atc1 = atc;
	}
	
	/**
	 * Sets the controller on COM2.
	 * @param atc a Controller bean
	 * @see ACARSRouteEntry#getATC2()
	 */
	public void setATC2(Controller atc) {
		_atc2 = atc;
	}
	
	/**
	 * Marks this route entry as having a notable flight parameter.
	 * @return TRUE if the entry should be noted, otherwise FALSE
	 */
	@Override
	public boolean isWarning() {
		return (getWarning() != null);
	}
	
	private String getATCData(int idx) {
		StringBuilder buf = new StringBuilder();
		Controller ctr = (idx == 1) ? _atc1 : _atc2;
		if (ctr != null) {
			buf.append("COM");
			buf.append(idx);
			buf.append(": ");
			buf.append((idx == 1) ? _com1 : _com2);
			buf.append(" (");
			buf.append(ctr.getCallsign());
			buf.append(')');
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the warning message.
	 * @return the warning
	 */
	@Override
	public String getWarning() {
		StringBuilder buf = new StringBuilder();
		if ((getAltitude() < 10000) && (getAirSpeed() > 250))
			buf.append("250 UNDER 10K ");
		if ((_radarAlt < 1500) && (_vSpeed < -1500))
			buf.append("DESCENT RATE ");
		if (Math.abs(_bank) > 45)
			buf.append("BANK ");
		if (Math.abs(_pitch) > 35)
			buf.append("PITCH ");
		if (Math.abs(1 - _gForce) >= 0.25)
			buf.append("G-FORCE ");
		if (getFuelRemaining() < 25)
			buf.append("NO FUEL ");
		if (isFlagSet(FLAG_STALL))
			buf.append("STALL ");
		if (isFlagSet(FLAG_OVERSPEED))
			buf.append("OVERSPEED ");
		if ((getAltitude() > 45000) && (getMach() < 1.05))
			buf.append("ALTITUDE ");
		if (isFlagSet(FLAG_GEARDOWN) && (getAirSpeed() > 250))
			buf.append("GEAR SPEED ");
		if (!isFlagSet(FLAG_GEARDOWN) && isFlagSet(FLAG_ONGROUND))
			buf.append("GEAR UP ");
		if (isFlagSet(FLAG_CRASH))
			buf.append("CRASH");
		
		if (buf.length() == 0)
			return null;
		
		buf.setLength(buf.length() -1);
		return buf.toString();
	}

	/**
	 * Return the Google Maps icon color.
	 */
	@Override
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
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder(256);
		buf.append("<div class=\"mapInfoBox acarsFlight\">Position: <span class=\"bld\">");
		buf.append(StringUtils.format(_pos, true, GeoLocation.ALL));
		buf.append("</span><br />Time: ");
		buf.append(StringUtils.format(getDate(), "MM/dd/yyyy HH:mm:ss"));
		buf.append(" UTC<br />Altitude: ");
		buf.append(StringUtils.format(getAltitude(), "#,000"));
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
		buf.append(StringUtils.format(getAirSpeed(), "##0"));
		buf.append(" kts (GS: ");
		buf.append(StringUtils.format(getGroundSpeed(), "#,##0"));
		buf.append(" kts)");
		if (getMach() > 0.6) {
			buf.append(" <i>Mach ");
			buf.append(StringUtils.format(getMach(), "0.00"));
			buf.append("</i>");
		}

		buf.append("<br />Heading: ");
		buf.append(StringUtils.format(getHeading(), "000"));
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
		if (isFlagSet(FLAG_SPARMED)) {
			buf.append("<span class=\"ita\">");
			buf.append(isFlagSet(FLAG_ONGROUND) ? "SPOILERS" : "SPEED BRAKES");
			buf.append("</span><br />");
		}

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
		else if (isFlagSet(FLAG_AT_FLCH))
			buf.append("Autothrottle: FLCH<br />");
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
			buf.append("</span><br />");
		}
		
		// Add Thrust Reverser flags if set
		if (isFlagSet(FLAG_REVERSETHRUST) && isFlagSet(FLAG_ONGROUND))
			buf.append("<span class=\"ita\">THRUST REVERSERS</span><br />");
		
		// Add ATC info
		buf.append(getATCData(1));
		if ((_atc1 != null) && (_atc2 != null)) buf.append("<br />");
		buf.append(getATCData(2));

		buf.append("</div>");
		return buf.toString();
	}
}