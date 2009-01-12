// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.Date;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store ServInfo data for historical purposes.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class PositionData implements GeospaceLocation, MarkerMapEntry, Comparable<PositionData> {
	
	private GeoPosition _pos;
	private Date _dt;
	private int _aSpeed;
	private int _hdg;
	
	private int _pilotID;
	private int _pirepID;
	private Airport _airportD;
	private Airport _airportA;
	private OnlineNetwork _net;

	/**
	 * Initializes the bean.
	 * @param net the Online Network used
	 * @param dt the date/time of the position entry 
	 */
	public PositionData(OnlineNetwork net, Date dt) {
		super();
		_net = net;
		_dt = dt;
	}

	/**
	 * Returns the aircraft's altitude.
	 */
	public int getAltitude() {
		return _pos.getAltitude();
	}

	/**
	 * Returns the aircraft's latitude.
	 */
	public double getLatitude() {
		return _pos.getLatitude();
	}

	/**
	 * Returns the aircraft's longitude.
	 */
	public double getLongitude() {
		return _pos.getLongitude();
	}

	/**
	 * Returns the date/time of the position entry.
	 * @return the date/time in UTC
	 */
	public Date getDate() {
		return _dt;
	}
	
	/**
	 * Returns the aircraft heading.
	 * @return the heading in degrees
	 */
	public int getHeading() {
		return _hdg;
	}
	
	/**
	 * Returns the aircraft's airspeed.
	 * @return the airspeed in knots
	 */
	public int getAirSpeed() {
		return _aSpeed;
	}
	
	/**
	 * Returns the Online Network used.
	 * @return the OnlineNetwork
	 */
	public OnlineNetwork getNetwork() {
		return _net;
	}
	
	/**
	 * Returns the Pilot ID.
	 * @return the Pilot's database ID
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns the Flight Report ID for this entry.
	 * @return the Flight Report database ID, or zero if none
	 */
	public int getFlightID() {
		return _pirepID;
	}
	
	/**
	 * Returns the departure Airport.
	 * @return the departure Airport
	 */
	public Airport getAirportD() {
		return _airportD;
	}
	
	/**
	 * Returns the arrival Airport.
	 * @return the arrival Airport
	 */
	public Airport getAirportA() {
		return _airportA;
	}
	
	/**
	 * Sets the position for this entry.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @param alt the altitude in feet MSL
	 */
	public void setPosition(double lat, double lng, int alt) {
		_pos = new GeoPosition(lat, lng);
		_pos.setAltitude(alt);
	}
	
	/**
	 * Updates the aircraft's heading.
	 * @param hdg the heading in degrees.
	 */
	public void setHeading(int hdg) {
		_hdg = hdg;
	}
	
	/**
	 * Updates the aircraft's airspeed.
	 * @param aSpeed the airspeed in knots
	 */
	public void setAirSpeed(int aSpeed) {
		_aSpeed = aSpeed;
	}
	
	/**
	 * Sets the Pilot ID for this entry.
	 * @param id the Pilot's database ID
	 */
	public void setPilotID(int id) {
		_pilotID = Math.max(0, id);
	}
	
	/**
	 * Sets the Flight Report ID for this entry.
	 * @param id the Flight Report's database ID
	 */
	public void setFlightID(int id) {
		_pirepID = Math.max(0, id);
	}

	/**
	 * Updates the departure airport.
	 * @param a the departure Airport
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Updates the arrival airport.
	 * @param a the arrival  Airport
	 */
	public void setAirportA(Airport a) {
		_airportA = a;	
	}

	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\"><b>");
		buf.append(StringUtils.format(_dt, "MM/dd/yyyy HH:mm:ss"));
		buf.append("</b><br /><br />Position: ");
		buf.append(StringUtils.format(_pos, true, GeoLocation.ALL));
		buf.append("<br />Altitude: ");
		buf.append(StringUtils.format(_pos.getAltitude(), "#,000"));
		buf.append(" feet<br />Airspeed:");
		buf.append(StringUtils.format(_aSpeed, "#,000"));
		buf.append(" knots<br />Heading:");
		buf.append(StringUtils.format(_hdg, "000"));
		buf.append(" degrees</span>");
		return buf.toString();
	}
	
	public String getIconColor() {
		if ((_pos.getAltitude() < 10000) && (_aSpeed > 250))
			return RED;
		return BLUE;
	}

	/**
	 * Compares two positions by comparing their date/time.
	 */
	public int compareTo(PositionData pd2) {
		return _dt.compareTo(pd2._dt);
	}
}