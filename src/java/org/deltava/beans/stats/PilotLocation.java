// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.Pilot;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.util.StringUtils;

/**
 * A bean to store pilot locations for displaying on a Google Map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotLocation implements MapEntry {
	
	private Pilot _usr;
	private GeoLocation _position;

	/**
	 * Creates a new Pilot location object.
	 * @param usr the Pilot bean
	 * @param loc the Pilot's location
	 */
	public PilotLocation(Pilot usr, GeoLocation loc) {
		super();
		_usr = usr;
		_position = loc;
	}
	
	/**
	 * Returns the Pilot's latitude.
	 * @return the latitude in degrees
	 * @see PilotLocation#getLongitude()
	 */
	public final double getLatitude() {
		return _position.getLatitude();
	}
	
	/**
	 * Returns the Pilot's longitude.
	 * @return the longitude in degrees
	 * @see PilotLocation#getLatitude()
	 */
	public final double getLongitude() {
		return _position.getLongitude();
	}
	
	/**
	 * Returns the hemispheres containing this Pilot.
	 * @return bit-wise hemisphere constants
	 * @see GeoLocation#getHemisphere()
	 */
	public final int getHemisphere() {
	   return _position.getHemisphere();
	}
	
	/**
	 * Returns the Pilot.
	 * @return the Pilot bean
	 */
	public Pilot getUser() {
		return _usr;
	}
	
	/**
	 * Display the Google Map icon color.
	 * @return BLUE
	 */
	public String getIconColor() {
		return MapEntry.BLUE;
	}
	
	/**
	 * Returns the Google Map info box text.
	 * @return the info box text
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"small\"><b>");
		buf.append(_usr.getName());
		buf.append("</b> (");
		buf.append(_usr.getPilotCode());
		buf.append(")<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />Location: ");
		buf.append(_usr.getLocation());
		buf.append("<br /><br />Position: ");
		buf.append(StringUtils.format(_position, true, GeoLocation.ALL));
		buf.append("<br />Joined on: ");
		buf.append(StringUtils.format(_usr.getCreatedOn(), "EEEE MMMM dd, yyyy"));
		buf.append("</span>");
		return buf.toString();
	}
}