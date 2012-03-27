// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.servinfo.Controller;
import org.deltava.beans.servinfo.Facility;

import org.deltava.util.StringUtils;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 4.1
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
	private int _fuelFlow;
	private int _flaps;
	private int _frameRate;
	private int _simRate;
	
	private String _com1;
	private Controller _atc;

	private static final int[] AP_FLAGS = { FLAG_AP_APR, FLAG_AP_HDG, FLAG_AP_NAV, FLAG_AP_ALT, FLAG_AP_GPS , FLAG_AP_LNAV};
	private static final String[] AP_FLAG_NAMES = { "APR", "HDG", "NAV", "ALT", "GPS", "LNAV" };
	
	/**
	 * Creates a new ACARS Route Entry bean.
	 * @param dt the date/time of this entry
	 * @param loc the aircraft's location
	 * @see ACARSRouteEntry#getDate()
	 * @see ACARSRouteEntry#getLocation()
	 */
	public ACARSRouteEntry(Date dt, GeoLocation loc) {
		super(loc, dt);
	}
	
	/**
	 * Returns the aircraft altitude above <i>ground level</i>.
	 * @return the altitude in feet AGL
	 * @see ACARSRouteEntry#setRadarAltitude(int)
	 */
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
	 * Returns the Flight Simulator time acceleration rate.
	 * @return the acceleration rate
	 * @see ACARSRouteEntry#setSimRate(int)
	 */
	public int getSimRate() {
		return _simRate;
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
	 * Returns the Controller on COM1. 
	 * @return a Controller bean, or null if none
	 * @see ACARSRouteEntry#setController(Controller)
	 */
	public Controller getController() {
		return _atc;
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
	 * Updates the Flight Simulator time acceleration rate.
	 * @param rate the rate
	 * @see ACARSRouteEntry#getSimRate()
	 */
	public void setSimRate(int rate) {
		_simRate = Math.max(1, rate);
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
	 * Sets the COM1 radio frequency.
	 * @param com1 the frequency
	 * @see ACARSRouteEntry#getCOM1()
	 */
	public void setCOM1(String com1) {
		_com1 = com1;
	}
	
	/**
	 * Sets the controller on COM1.
	 * @param atc a Controller bean
	 * @see ACARSRouteEntry#getController()
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
		if ((getAltitude() < 10000) && (getAirSpeed() > 250))
			warnings.add("250 UNDER 10K");
		if ((_radarAlt < 1500) && (_vSpeed < -1500))
			warnings.add("DESCENT RATE");
		if (Math.abs(_bank) > 45)
			warnings.add("BANK");
		if (Math.abs(_pitch) > 35)
			warnings.add("PITCH");
		if (Math.abs(1 - _gForce) >= 0.25)
			warnings.add("G-FORCE");
		if (getFuelRemaining() < 25)
			warnings.add("NO FUEL");
		if (isFlagSet(FLAG_STALL))
			warnings.add("STALL");
		if (isFlagSet(FLAG_OVERSPEED))
			warnings.add("OVERSPEED");
		if ((getAltitude() > 45000) && (getMach() < 1.05))
			warnings.add("ALTITUDE");
		if (isFlagSet(FLAG_GEARDOWN) && (getAirSpeed() > 250))
			warnings.add("GEAR SPEED");
		if (!isFlagSet(FLAG_GEARDOWN) && isFlagSet(FLAG_ONGROUND))
			warnings.add("GEAR UP");
		if (isFlagSet(FLAG_CRASH))
			warnings.add("CRASH");
		
		return warnings.isEmpty() ? null : StringUtils.listConcat(warnings, " ");
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
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		_radarAlt = in.readInt();
		_pitch = in.readFloat();
		_bank = in.readFloat();
		_vSpeed = in.readShort();
		_aoa = in.readFloat();
		_gForce = in.readFloat();
		_n1 = in.readFloat();
		_n2 = in.readFloat();
		_viz = in.readFloat();
		_fuelFlow = in.readInt();
		_flaps = in.readShort();
		_frameRate = in.readShort();
		_simRate = in.readShort();
		
		// Check for controller
		String com1 = in.readUTF();
		if (!StringUtils.isEmpty(com1)) {
			_com1 = com1;
			_atc = new Controller(in.readInt());
			_atc.setFacility(Facility.values()[in.readShort()]);
			_atc.setCallsign(in.readUTF());
			_atc.setPosition(in.readFloat(), in.readFloat());
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(_radarAlt);
		out.writeFloat((float) _pitch);
		out.writeFloat((float) _bank);
		out.writeShort(_vSpeed);
		out.writeFloat((float) _aoa);
		out.writeFloat((float) _gForce);
		out.writeFloat((float) _n1);
		out.writeFloat((float) _n2);
		out.writeFloat((float) _viz);
		out.writeInt(_fuelFlow);
		out.writeShort(_flaps);
		out.writeShort(_frameRate);
		out.writeShort(_simRate);
		
		// Write controller
		if (_atc != null) {
			out.writeUTF(_com1);
			out.writeInt(_atc.getID());
			out.writeShort(_atc.getFacility().ordinal());
			out.writeUTF(_atc.getCallsign());
			out.writeFloat((float) _atc.getLatitude());
			out.writeFloat((float) _atc.getLongitude());
		} else
			out.writeUTF("");
	}
}