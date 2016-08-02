// Copyright 2009, 2010, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;

/**
 * A bean to store ServInfo data for historical purposes.
 * @author Luke
 * @version 7.0
 * @since 2.4
 */

public class PositionData implements GeospaceLocation, MarkerMapEntry, Comparable<PositionData> {
	
	private GeoPosition _pos;
	private final Instant _dt;
	private int _aSpeed;
	private int _hdg;
	
	private int _pilotID;
	private int _pirepID;

	/**
	 * Initializes the bean.
	 * @param dt the date/time of the position entry 
	 */
	public PositionData(Instant dt) {
		super();
		_dt = dt;
	}

	/**
	 * Returns the aircraft's altitude.
	 */
	@Override
	public int getAltitude() {
		return _pos.getAltitude();
	}

	/**
	 * Returns the aircraft's latitude.
	 */
	@Override
	public double getLatitude() {
		return _pos.getLatitude();
	}

	/**
	 * Returns the aircraft's longitude.
	 */
	@Override
	public double getLongitude() {
		return _pos.getLongitude();
	}

	/**
	 * Returns the date/time of the position entry.
	 * @return the date/time in UTC
	 */
	public Instant getDate() {
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
	 * Sets the position for this entry.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @param alt the altitude in feet MSL
	 */
	public void setPosition(double lat, double lng, int alt) {
		_pos = new GeoPosition(lat, lng, alt);
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

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox onlinePilot\"><span class=\"bld\">");
		buf.append(StringUtils.format(_dt, "MM/dd/yyyy HH:mm:ss"));
		buf.append("</span><br /><br />Position: ");
		buf.append(StringUtils.format(_pos, true, GeoLocation.ALL));
		buf.append("<br />Altitude: ");
		buf.append(StringUtils.format(_pos.getAltitude(), "#,000"));
		buf.append(" feet<br />Speed: ");
		buf.append(StringUtils.format(_aSpeed, "#,000"));
		buf.append(" knots<br />Heading: ");
		buf.append(StringUtils.format(_hdg, "000"));
		buf.append(" degrees</div>");
		return buf.toString();
	}
	
	@Override
	public String getIconColor() {
		if ((_pos.getAltitude() < 10000) && (_aSpeed > 275))
			return RED;
		return BLUE;
	}

	/**
	 * Compares two positions by comparing their date/time.
	 */
	@Override
	public int compareTo(PositionData pd2) {
		int tmpResult = _dt.compareTo(pd2._dt);
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_pilotID).compareTo(Integer.valueOf(pd2._pilotID));
		
		return tmpResult;
	}
	
	@Override
	public int hashCode() {
		return (String.valueOf(_pilotID) + _dt.toString()).hashCode(); 
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof PositionData) && (compareTo((PositionData) o) == 0));
	}
}